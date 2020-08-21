package com.littlebayreal.easysocketlib.iothreads;


import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.base.AbsLoopThread;
import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.base.EasySocketOptions;
import com.littlebayreal.easysocketlib.client.delegate.io.ReaderImpl;
import com.littlebayreal.easysocketlib.client.delegate.io.WriterImpl;
import com.littlebayreal.easysocketlib.exceptions.ManuallyDisconnectException;
import com.littlebayreal.easysocketlib.interfaces.io.IIOManager;
import com.littlebayreal.easysocketlib.interfaces.io.IReader;
import com.littlebayreal.easysocketlib.interfaces.io.IStateSender;
import com.littlebayreal.easysocketlib.interfaces.io.IWriter;
import com.littlebayreal.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.littlebayreal.easysocketlib.interfaces.send.ISendable;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by xuhao on 2017/5/31.
 */

public class IOThreadManager implements IIOManager<EasySocketOptions> {

    protected InputStream mInputStream;

    protected OutputStream mOutputStream;

	protected ConnectionInfo mConnectionInfo;

    private volatile EasySocketOptions mOkOptions;

    protected IStateSender mSender;

    protected IReader mReader;

	protected IWriter mWriter;

    private AbsLoopThread mSimplexThread;

    private DuplexReadThread mDuplexReadThread;

    private DuplexWriteThread mDuplexWriteThread;

    private EasySocketOptions.IOThreadMode mCurrentThreadMode;

    public IOThreadManager(InputStream inputStream,
						   OutputStream outputStream,
						   EasySocketOptions okOptions,
						   IStateSender stateSender) {
       this(null,inputStream,outputStream,okOptions,stateSender);
    }
	public IOThreadManager(ConnectionInfo connectionInfo, InputStream inputStream,
						   OutputStream outputStream,
						   EasySocketOptions okOptions,
						   IStateSender stateSender) {
		mConnectionInfo = connectionInfo;
		mInputStream = inputStream;
		mOutputStream = outputStream;
		mOkOptions = okOptions;
		mSender = stateSender;
		initIO();
	}
    public void initIO() {
    	//检测协议头是否正常
        assertHeaderProtocolNotEmpty();
        mReader = new ReaderImpl(mConnectionInfo != null?mConnectionInfo.getConnectionName():"");
        mReader.initialize(mInputStream, mSender);
        mWriter = new WriterImpl(mConnectionInfo != null?mConnectionInfo.getConnectionName():"");
        mWriter.initialize(mOutputStream, mSender);
    }

    @Override
    public synchronized void startEngine() {
        mCurrentThreadMode = mOkOptions.getIOThreadMode();
        //初始化读写工具类
        mReader.setOption(mOkOptions);
        mWriter.setOption(mOkOptions);
        switch (mOkOptions.getIOThreadMode()) {
            case DUPLEX:
                SLog.w("DUPLEX is processing");
                duplex();
                break;
            case SIMPLEX:
                SLog.w("SIMPLEX is processing");
                simplex();
                break;
            default:
                throw new RuntimeException("未定义的线程模式");
        }
    }

	/**
	 * 双工模式
	 */
	private void duplex() {
        shutdownAllThread(null);
        mDuplexWriteThread = new DuplexWriteThread(mWriter, mSender);
        mDuplexReadThread = new DuplexReadThread(mReader, mSender);
        mDuplexWriteThread.start();
        mDuplexReadThread.start();
    }

	/**
	 * 单工模式
	 */
	private void simplex() {
        shutdownAllThread(null);
        mSimplexThread = new SimplexIOThread(mReader, mWriter, mSender);
        mSimplexThread.start();
    }

    private void shutdownAllThread(Exception e) {
        if (mSimplexThread != null) {
            mSimplexThread.shutdown(e);
            mSimplexThread = null;
        }
        if (mDuplexReadThread != null) {
            mDuplexReadThread.shutdown(e);
            mDuplexReadThread = null;
        }
        if (mDuplexWriteThread != null) {
            mDuplexWriteThread.shutdown(e);
            mDuplexWriteThread = null;
        }
    }

    @Override
    public synchronized void setOkOptions(EasySocketOptions options) {
        mOkOptions = options;
        if (mCurrentThreadMode == null) {
            mCurrentThreadMode = mOkOptions.getIOThreadMode();
        }
        assertTheThreadModeNotChanged();
        assertHeaderProtocolNotEmpty();

        mWriter.setOption(mOkOptions);
        mReader.setOption(mOkOptions);
    }

    @Override
    public void send(ISendable sendable) {
        mWriter.offer(sendable);
    }

    @Override
    public void close() {
        close(new ManuallyDisconnectException());
    }

    @Override
    public synchronized void close(Exception e) {
        shutdownAllThread(e);
        mCurrentThreadMode = null;
    }

    public void assertHeaderProtocolNotEmpty() {
        IReaderProtocol protocol = mOkOptions.getReaderProtocol();
        if (protocol == null) {
            throw new IllegalArgumentException("The reader protocol can not be Null.");
        }

        if (protocol.getHeaderLength() == 0) {
            throw new IllegalArgumentException("The header length can not be zero.");
        }
    }

    private void assertTheThreadModeNotChanged() {
        if (mOkOptions.getIOThreadMode() != mCurrentThreadMode) {
            throw new IllegalArgumentException("can't hot change iothread mode from " + mCurrentThreadMode + " to "
                    + mOkOptions.getIOThreadMode() + " in blocking io manager");
        }
    }

}
