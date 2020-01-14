package com.sziti.easysocketlib.protocol;

import com.sziti.easysocketlib.interfaces.protocol.IReaderProtocol;

import java.nio.ByteOrder;

public class Jt808ReaderProtocol implements IReaderProtocol {
	@Override
	public int getHeaderLength() {
		return 0;
	}

	@Override
	public int getBodyLength(byte[] header, ByteOrder byteOrder) {
		return 0;
	}
}
