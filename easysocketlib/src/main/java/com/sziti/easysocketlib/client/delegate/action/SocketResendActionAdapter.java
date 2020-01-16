package com.sziti.easysocketlib.client.delegate.action;

import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.interfaces.action.IAction;
import com.sziti.easysocketlib.interfaces.action.IOResendAction;
import com.sziti.easysocketlib.interfaces.action.ISocketResendActionListener;
import com.sziti.easysocketlib.interfaces.send.IPulseSendable;
import com.sziti.easysocketlib.interfaces.send.ISendable;

public abstract class SocketResendActionAdapter implements ISocketResendActionListener {
	/**
	 * Socket通讯发送失败后的回调<br>
	 * @param action {@link IOResendAction#ACTION_RESEND_REQUEST}
	 * @param data { 写出的数据{@link BaseSendData}}
	 */
	@Override
	public void onSocketWriteFailed(String action, BaseSendData data) {

	}
}
