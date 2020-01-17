package com.sziti.easysocketlib.client.delegate.io;
import com.sziti.easysocketlib.protocol.CommonReaderProtocol;

public class ResendReaderImpl extends AbsReader {
	@Override
	public void read() throws RuntimeException {
		//获取数据协议
		CommonReaderProtocol commonReaderProtocol = (CommonReaderProtocol)mOkOptions.getReaderProtocol();
	}
}
