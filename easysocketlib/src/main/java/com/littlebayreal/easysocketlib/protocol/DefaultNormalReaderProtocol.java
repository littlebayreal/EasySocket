package com.littlebayreal.easysocketlib.protocol;


import com.littlebayreal.easysocketlib.interfaces.protocol.IReaderProtocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 对数据报文解析的默认实现
 */
public class DefaultNormalReaderProtocol implements IReaderProtocol {

    @Override
    public int getHeaderLength() {
        return 4;
    }

	@Override
	public int getHeaderLength(byte[] data) {
		return 0;
	}

	@Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        if (header == null || header.length < getHeaderLength()) {
            return 0;
        }
        ByteBuffer bb = ByteBuffer.wrap(header);
        bb.order(byteOrder);
        return bb.getInt();
    }
}
