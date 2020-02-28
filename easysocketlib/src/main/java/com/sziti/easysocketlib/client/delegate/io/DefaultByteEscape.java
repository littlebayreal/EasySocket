package com.sziti.easysocketlib.client.delegate.io;

import com.sziti.easysocketlib.interfaces.protocol.IByteEscape;
import com.sziti.easysocketlib.util.ByteEscapeHelper;

public class DefaultByteEscape implements IByteEscape {
	@Override
	public byte[] encodeBytes(byte[] bytes, int offset, int length) throws Exception {
		return ByteEscapeHelper.doEscape4Send(bytes,offset,length);
	}

	@Override
	public byte[] decodeBytes(byte[] bytes, int offset, int length) throws Exception {
		return ByteEscapeHelper.doEscape4Receive(bytes,offset,length);
	}
}
