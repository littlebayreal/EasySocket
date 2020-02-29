package com.littlebayreal.easysocketlib.interfaces.protocol;

public interface IHeaderProtocol {
	/**
	 * 设置body的长度
	 */
	int setBodyLength(int bodyLength);
	/**
	 * 设置流水号
	 */
	void setSerialNum(int serialNum);
	/**
	 * 组装生成头字节
	 * @return
	 */
	byte[] getHeaderBytes();
}
