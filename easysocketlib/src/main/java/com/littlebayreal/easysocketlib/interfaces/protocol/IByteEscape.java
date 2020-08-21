package com.littlebayreal.easysocketlib.interfaces.protocol;

/**
 * 2020/8/21 create by LiTtleBayReal
 * 转义策略实现接口
 */
public interface IByteEscape {

	/**
	 * 对传入的bytes数组进行传输前的转义
	 * @return
	 */
	byte[] encodeBytes(byte[] bytes, int offset, int length) throws Exception;

	/**
	 * 对传入的bytes数组进行解析之前的转义
	 * @param bytes
	 * @return
	 */
	byte[] decodeBytes(byte[] bytes, int offset, int length) throws Exception;
}
