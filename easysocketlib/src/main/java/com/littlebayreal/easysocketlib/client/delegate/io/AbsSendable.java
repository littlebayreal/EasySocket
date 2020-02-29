package com.littlebayreal.easysocketlib.client.delegate.io;

import com.littlebayreal.easysocketlib.interfaces.send.ISendable;

public class AbsSendable implements ISendable {
	//流水号
	protected int mSerialNum;

	public void setSerialNum(int serialNum){
		this.mSerialNum = serialNum;
	}
	@Override
	public byte[] parse() {
		return new byte[0];
	}
}
