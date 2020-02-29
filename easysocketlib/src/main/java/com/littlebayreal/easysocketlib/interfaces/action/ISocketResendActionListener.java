package com.littlebayreal.easysocketlib.interfaces.action;

import com.littlebayreal.easysocketlib.client.pojo.BaseSendData;

/**
 * 针对补发功能的监听
 * create by LiTtleBayReal
 */
public interface ISocketResendActionListener extends ISocketActionListener{
	/**
	 * Socket通讯发送失败后的回调<br>
	 * @param action {@link IOResendAction#ACTION_RESEND_REQUEST}
	 * @param data { 写出的数据{@link BaseSendData}}
	 */
	void onSocketWriteFailed(String action, BaseSendData data);
}
