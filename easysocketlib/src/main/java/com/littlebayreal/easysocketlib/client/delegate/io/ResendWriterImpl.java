package com.littlebayreal.easysocketlib.client.delegate.io;

import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.exceptions.WriteException;
import com.littlebayreal.easysocketlib.interfaces.action.IOAction;
import com.littlebayreal.easysocketlib.interfaces.action.IOResendAction;
import com.littlebayreal.easysocketlib.interfaces.io.IIOCoreOptions;
import com.littlebayreal.easysocketlib.interfaces.io.IStateSender;
import com.littlebayreal.easysocketlib.interfaces.io.IWriter;
import com.littlebayreal.easysocketlib.interfaces.send.IPulseSendable;
import com.littlebayreal.easysocketlib.interfaces.send.ISendable;
import com.littlebayreal.easysocketlib.util.HexStringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * create by LiTtleBayReal
 * 针对补传进行特殊处理的io输出类
 */
public class ResendWriterImpl implements IWriter {
	protected String mThreadName;

    private volatile IIOCoreOptions mOkOptions;

    private IStateSender mStateSender;

    private OutputStream mOutputStream;

    private LinkedBlockingQueue<ISendable> mQueue = new LinkedBlockingQueue<>();

	public ResendWriterImpl(String threadName){
		mThreadName = threadName;
	}
    @Override
    public void initialize(OutputStream outputStream, IStateSender stateSender) {
        mStateSender = stateSender;
        mOutputStream = outputStream;
    }

    @Override
    public boolean write() throws RuntimeException {
        ISendable sendable = null;
        try {
            sendable = mQueue.take();
        } catch (InterruptedException e) {
            //ignore;
        }
        if (sendable != null) {
            try {
                byte[] sendBytes;
                if (mOkOptions.getWriterProtocol().isOpenEscape() && mOkOptions.getWriterProtocol().getIByteEscape() != null)
                    sendBytes = sendable.parse(mOkOptions.getWriterProtocol());
                else
                    sendBytes = sendable.parse();
                int packageSize = mOkOptions.getWritePackageBytes();
                int remainingCount = sendBytes.length;
                ByteBuffer writeBuf = ByteBuffer.allocate(packageSize);
                writeBuf.order(mOkOptions.getWriteByteOrder());
                int index = 0;
                while (remainingCount > 0) {
                    int realWriteLength = Math.min(packageSize, remainingCount);
                    writeBuf.clear();
                    writeBuf.rewind();
                    writeBuf.put(sendBytes, index, realWriteLength);
                    writeBuf.flip();
                    byte[] writeArr = new byte[realWriteLength];
                    writeBuf.get(writeArr);
                    mOutputStream.write(writeArr);
                    mOutputStream.flush();

                    if (SLog.isDebug()) {
						SLog.i(mThreadName + " write bytes: " + HexStringUtils.toHexString(sendBytes));
						SLog.i(mThreadName + " bytes write length:" + realWriteLength);
                    }
                    index += realWriteLength;
                    remainingCount -= realWriteLength;
                }
                if (sendable instanceof IPulseSendable) {
                    mStateSender.sendBroadcast(IOAction.ACTION_PULSE_REQUEST, sendable);
                } else {
                    mStateSender.sendBroadcast(IOAction.ACTION_WRITE_COMPLETE, sendable);
                }
            } catch (Exception e) {
                SLog.e("write------e:" + e.getMessage());
                //网络异常需要补发消息
                if (sendable != null)
                    mStateSender.sendBroadcast(IOResendAction.ACTION_RESEND_REQUEST, sendable);
                WriteException writeException = new WriteException(e);
                throw writeException;
            }
            return true;
        }
        return false;
    }

    @Override
    public void setOption(IIOCoreOptions option) {
        mOkOptions = option;
    }

    @Override
    public void offer(ISendable sendable) {
        mQueue.offer(sendable);
    }

    @Override
    public void close() {
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
