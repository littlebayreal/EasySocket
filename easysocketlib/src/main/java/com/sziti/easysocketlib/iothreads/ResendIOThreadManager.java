package com.sziti.easysocketlib.iothreads;

import com.sziti.easysocketlib.base.EasySocketOptions;
import com.sziti.easysocketlib.client.delegate.io.ResendReaderImpl;
import com.sziti.easysocketlib.client.delegate.io.ResendWriterImpl;
import com.sziti.easysocketlib.interfaces.io.IStateSender;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 带有重发功能的io读写管理
 * create by LiTtleBayReal
 */
public class ResendIOThreadManager extends IOThreadManager{
	public ResendIOThreadManager(InputStream inputStream, OutputStream outputStream, EasySocketOptions okOptions, IStateSender stateSender) {
		super(inputStream, outputStream, okOptions, stateSender);
	}

	@Override
	public void initIO() {
		//检测协议头是否正常
		assertHeaderProtocolNotEmpty();
		mReader = new ResendReaderImpl();
		mReader.initialize(mInputStream, mSender);
		mWriter = new ResendWriterImpl();
		mWriter.initialize(mOutputStream, mSender);
	}
}
