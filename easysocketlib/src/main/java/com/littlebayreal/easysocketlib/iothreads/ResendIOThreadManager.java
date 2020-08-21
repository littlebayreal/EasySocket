package com.littlebayreal.easysocketlib.iothreads;
import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.base.EasySocketOptions;
import com.littlebayreal.easysocketlib.client.delegate.io.ResendReaderImpl;
import com.littlebayreal.easysocketlib.client.delegate.io.ResendWriterImpl;
import com.littlebayreal.easysocketlib.interfaces.io.IStateSender;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 带有重发功能的io读写管理
 * create by LiTtleBayReal
 */
public class ResendIOThreadManager extends IOThreadManager {
	public ResendIOThreadManager(InputStream inputStream, OutputStream outputStream, EasySocketOptions okOptions, IStateSender stateSender) {
		super(inputStream, outputStream, okOptions, stateSender);
	}
	public ResendIOThreadManager(ConnectionInfo connectionInfo, InputStream inputStream, OutputStream outputStream,
								 EasySocketOptions okOptions, IStateSender stateSender){
		super(connectionInfo,inputStream, outputStream, okOptions, stateSender);
	}
	@Override
	public void initIO() {
		//检测协议头是否正常
		assertHeaderProtocolNotEmpty();
		mReader = new ResendReaderImpl(mConnectionInfo != null?mConnectionInfo.getConnectionName():"");
		mReader.initialize(mInputStream, mSender);
		mWriter = new ResendWriterImpl(mConnectionInfo != null?mConnectionInfo.getConnectionName():"");
		mWriter.initialize(mOutputStream, mSender);
	}
}
