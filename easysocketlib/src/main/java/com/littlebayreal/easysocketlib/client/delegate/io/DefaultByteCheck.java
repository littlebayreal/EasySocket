package com.littlebayreal.easysocketlib.client.delegate.io;

import com.littlebayreal.easysocketlib.interfaces.protocol.IByteCheck;
import com.littlebayreal.easysocketlib.util.BitOperator;

/**
 * 2020/8/21 create by LiTtleBayReal
 * 默认的校验位实现策略
 */
public class DefaultByteCheck implements IByteCheck {
	@Override
	public int calculateCheckNum(byte[] bytes, int offset, int length) {
		return BitOperator.getCheckSum4JT808(bytes,offset,length)&0xff;
	}

	@Override
	public int getCheckOffset() {
		return 1;
	}

	@Override
	public int getCheckLength(byte[] bytes) {
		return bytes.length - 2;
	}

	@Override
	public int getCheckInData(byte[] bytes) {
		return bytes[bytes.length - 2] & 0xff;
	}
}
