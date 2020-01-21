package com.sziti.easysocketlib.interfaces.protocol;

public interface IByteEscape {
	/**
	 * 对传入的bytes数组进行传输前的转义
	 * @return
	 */
	byte[] encodeBytes(byte[] bytes,int offset,int length);

	/**
	 * 对传入的bytes数组进行解析之前的转义
	 * @param bytes
	 * @return
	 */
	byte[] decodeBytes(byte[] bytes,int offset,int length);
}
