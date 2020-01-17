package com.sziti.easysocketlib.client.dispatcher;

import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.client.delegate.action.AbsSocketResendHandler;
import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.client.pojo.OriginalData;

/**
 * 对补发事件的默认逻辑处理 作为用户可继承DefaultResendActionHandler重写处理方式
 * create by LiTtleBayReal
 */
public class DefaultResendActionHandler extends AbsSocketResendHandler {

	public DefaultResendActionHandler() {

	}
    //当重新连接成功，可以继续补发线程的运行
	@Override
	public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
		//开始补发操作
       iResend.startEngine();
	}

	//可做补发数据的添加操作
	@Override
	public void onSocketWriteFailed(String action, BaseSendData data) {
//		super.onSocketWriteFailed(action, data);
		//发送失败的数据也要放入补发队列
		iResend.add(data);
	}
    //可做补发数据的移除操作
	@Override
	public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
//		super.onSocketReadResponse(info, action, data);
		//根据协议号和流水号进行已发送数据的补发移除
		iResend.remove(data);
	}
}
