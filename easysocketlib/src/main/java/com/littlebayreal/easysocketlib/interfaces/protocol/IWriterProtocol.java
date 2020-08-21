package com.littlebayreal.easysocketlib.interfaces.protocol;
/**
 * 2020/8/21 create by LiTtleBayReal
 * 发送消息协议格式
 */
public interface IWriterProtocol {
	/**
	 * 是否开启发送转义
	 */
	boolean isOpenEscape();

	/**
	 * 获取设置的转义规则
	 */
	IByteEscape getIByteEscape();

	/**
	 * 获取转义开始位置
	 * @return
	 */
	int getEscapeStart();

	/**
	 * 获取转义结束位置
	 * @return
	 */
	int getEscapeEnd(byte[] bytes);
}
