package com.littlebayreal.easysocketlib.server.impl.iocore;

import com.littlebayreal.easysocketlib.client.delegate.io.ReaderImpl;
import com.littlebayreal.easysocketlib.client.delegate.io.ResendReaderImpl;
import com.littlebayreal.easysocketlib.client.delegate.io.ResendWriterImpl;
import com.littlebayreal.easysocketlib.client.delegate.io.WriterImpl;
import com.littlebayreal.easysocketlib.interfaces.io.IIOManager;
import com.littlebayreal.easysocketlib.interfaces.io.IReader;
import com.littlebayreal.easysocketlib.interfaces.io.IStateSender;
import com.littlebayreal.easysocketlib.interfaces.io.IWriter;
import com.littlebayreal.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.littlebayreal.easysocketlib.interfaces.send.ISendable;
import com.littlebayreal.easysocketlib.protocol.CommonReaderProtocol;
import com.littlebayreal.easysocketlib.server.exceptions.InitiativeDisconnectException;
import com.littlebayreal.easysocketlib.server.impl.EasyServerOptions;

import java.io.InputStream;
import java.io.OutputStream;

public class ClientIOManager implements IIOManager<EasyServerOptions> {
    private InputStream mInputStream;

    private OutputStream mOutputStream;

    private EasyServerOptions mOptions;

    private IStateSender mClientStateSender;

    private IReader mReader;

    private IWriter mWriter;

    private ClientReadThread mClientReadThread;

    private ClientWriteThread mClientWriteThread;

    public ClientIOManager(
            InputStream inputStream,
            OutputStream outputStream,
            EasyServerOptions okOptions,
            IStateSender clientStateSender) {
        mInputStream = inputStream;
        mOutputStream = outputStream;
        mOptions = okOptions;
        mClientStateSender = clientStateSender;
        initIO();
    }

    private void initIO() {
    	//检测头协议是否正常
        assertHeaderProtocolNotEmpty();
		if (mOptions.getReaderProtocol() instanceof CommonReaderProtocol){
			mReader = new ResendReaderImpl("server");
			mWriter = new ResendWriterImpl("server");
		}else {
			mReader = new ReaderImpl("server");
			mWriter = new WriterImpl("server");
		}
        setOkOptions(mOptions);

        mReader.initialize(mInputStream, mClientStateSender);
        mWriter.initialize(mOutputStream, mClientStateSender);
    }

    @Override
    public void startEngine() {
        // do nothing
    }

    public void startReadEngine() {
        if (mClientReadThread != null) {
            mClientReadThread.shutdown();
            mClientReadThread = null;
        }
        mClientReadThread = new ClientReadThread(mReader, mClientStateSender);
        mClientReadThread.start();
    }

    public void startWriteEngine() {
        if (mClientWriteThread != null) {
            mClientWriteThread.shutdown();
            mClientWriteThread = null;
        }
        mClientWriteThread = new ClientWriteThread(mWriter, mClientStateSender);
        mClientWriteThread.start();
    }

    private void shutdownAllThread(Exception e) {
        if (mClientReadThread != null) {
            mClientReadThread.shutdown(e);
            mClientReadThread = null;
        }
        if (mClientWriteThread != null) {
            mClientWriteThread.shutdown(e);
            mClientWriteThread = null;
        }
    }

    @Override
    public void setOkOptions(EasyServerOptions options) {
        mOptions = options;

        assertHeaderProtocolNotEmpty();
        if (mWriter != null && mReader != null) {
            mWriter.setOption(mOptions);
            mReader.setOption(mOptions);
        }
    }

    @Override
    public void send(ISendable sendable) {
        mWriter.offer(sendable);
    }

    @Override
    public void close() {
        close(new InitiativeDisconnectException());
    }

    @Override
    public void close(Exception e) {
        shutdownAllThread(e);
    }

    private void assertHeaderProtocolNotEmpty() {
        IReaderProtocol protocol = mOptions.getReaderProtocol();
        if (protocol == null) {
            throw new IllegalArgumentException("The reader protocol can not be Null.");
        }

        if (protocol.getHeaderLength() == 0) {
            throw new IllegalArgumentException("The header length can not be zero.");
        }
    }
}
