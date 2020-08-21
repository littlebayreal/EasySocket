package com.littlebayreal.easysocketlib.protocol;

import com.littlebayreal.easysocketlib.interfaces.protocol.IByteEscape;
import com.littlebayreal.easysocketlib.interfaces.protocol.IWriterProtocol;

/**
 * 一个默认实现的通用发送协议  用户可以继承并自定义
 */
public class CommonWriterProtocol implements IWriterProtocol {
	private int escapeStart = 0;
	private boolean isOpenEscape = false;
	private IByteEscape mByteEscape;
	public CommonWriterProtocol(boolean isOpenEscape, int escapeStart, IByteEscape mByteEscape){
		this.isOpenEscape = isOpenEscape;
		this.escapeStart = escapeStart;
		this.mByteEscape = mByteEscape;
	}
	@Override
	public boolean isOpenEscape() {
		return isOpenEscape;
	}

	@Override
	public IByteEscape getIByteEscape() {
		return mByteEscape;
	}

	@Override
	public int getEscapeStart() {
		return escapeStart;
	}

	@Override
	public int getEscapeEnd(byte[] bytes) {
		return bytes.length - 1;
	}
}
