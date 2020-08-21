package com.littlebayreal.easysocketlib.protocol;

import com.littlebayreal.easysocketlib.interfaces.protocol.IByteEscape;
import com.littlebayreal.easysocketlib.interfaces.protocol.IWriterProtocol;

public class DefaultNormalWriterProtocol implements IWriterProtocol {
	@Override
	public boolean isOpenEscape() {
		return false;
	}

	@Override
	public IByteEscape getIByteEscape() {
		return null;
	}

	@Override
	public int getEscapeStart() {
		return 0;
	}

	@Override
	public int getEscapeEnd(byte[] bytes) {
		return 0;
	}
}
