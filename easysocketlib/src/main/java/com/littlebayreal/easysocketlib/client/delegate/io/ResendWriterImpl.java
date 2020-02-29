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
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * create by LiTtleBayReal
 * 针对补传进行特殊处理的io输出类
 */
public class ResendWriterImpl implements IWriter{
	private volatile IIOCoreOptions mOkOptions;

	private IStateSender mStateSender;

	private OutputStream mOutputStream;
	//发送的流水号
//	private int mSerialNum;
	private LinkedBlockingQueue<ISendable> mQueue = new LinkedBlockingQueue<>();
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
				//根据配置 设置报文流水号
				byte[] sendBytes = sendable.parse();
				//发送之前 添加正确的流水号 作为移除时判断依据
//				if (mOkOptions.getReaderProtocol() instanceof CommonReaderProtocol){
//					SLog.i("修改流水号:"+ mSerialNum);
//					((AbsSendable)sendable).setSerialNum(mSerialNum);
//					sendBytes = sendable.parse();
//				}
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
						byte[] forLogBytes = Arrays.copyOfRange(sendBytes, index, index + realWriteLength);
                        SLog.i("write bytes: " + HexStringUtils.toHexString(forLogBytes));
						SLog.i("bytes write length:" + realWriteLength);
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
				//网络异常需要补发消息
				if (sendable != null)
					mStateSender.sendBroadcast(IOResendAction.ACTION_RESEND_REQUEST,sendable);
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
//		mSerialNum++;
//		if (mSerialNum > 0xffff)mSerialNum = 1;
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
