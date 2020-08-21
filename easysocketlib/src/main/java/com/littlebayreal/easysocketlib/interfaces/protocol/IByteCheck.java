package com.littlebayreal.easysocketlib.interfaces.protocol;
/**
 * 2020/8/21 create by LiTtleBayReal
 * 校验位实现策略接口
 */
public interface IByteCheck {
	/**
	 * 根据转义策略计算转义结果
	 */
	int calculateCheckNum(byte[] bytes, int offset, int length);

	/**
	 * 获取转义开始位置
	 * @return
	 */
	int getCheckOffset();

	/**
	 * 获取转义结束位置
	 * @return
	 */
	int getCheckLength(byte[] bytes);

	/**
	 * 获取报文中的校验值
	 */
	int getCheckInData(byte[] bytes);
}
