package com.littlebayreal.easysocketlib.util;


/**
 * 流水号生成器
 */
public class SerialNumGen {
	private int mSerialNum = 0;
	private static class InstanceHolder {
		private static final SerialNumGen INSTANCE = new SerialNumGen();
	}
	public static SerialNumGen getInstance() {
		return SerialNumGen.InstanceHolder.INSTANCE;
	}
	public int getSerialNum(){
		if (mSerialNum > 0xffff)mSerialNum = 0;
		return mSerialNum++;
	}
}
