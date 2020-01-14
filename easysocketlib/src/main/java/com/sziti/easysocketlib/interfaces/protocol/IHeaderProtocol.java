package com.sziti.easysocketlib.interfaces.protocol;

public interface IHeaderProtocol {
	/**
	 * 设置报文流水号
	 */
	void setSerialNum(int serialNum);
	/**
	 * 设置body的长度
	 */
	int setBodyLength(int bodyLength);
	/**
	 * 组装生成头字节
	 * @return
	 */
	byte[] getHeaderBytes();

}
