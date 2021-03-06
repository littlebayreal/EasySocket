package com.littlebayreal.easysocketlib.client.delegate.io;

import com.littlebayreal.easysocketlib.interfaces.protocol.IByteEscape;
import com.littlebayreal.easysocketlib.util.ByteEscapeHelper;

/**
 * 2020/8/21 create by LiTtleBayReal
 * 默认的转义策略
 */
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
