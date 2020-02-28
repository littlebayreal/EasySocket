package com.sziti.easysocketlib.client.dispatcher;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.client.delegate.action.AbsSocketResendHandler;
import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.util.BitOperator;

/**
 * 对补发事件的默认逻辑处理 作为用户可继承AbsSocketResendHandler重写处理方式
 * create by LiTtleBayReal
 */
public class DefaultResendActionHandler extends AbsSocketResendHandler {

	public DefaultResendActionHandler() {

	}
    //当重新连接成功，可以继续补发线程的运行
	@Override
	public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
		SLog.e("IResend对象:"+ iResend.toString());
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
		//根据设置的条件进行已发送数据的补发移除
        byte[] item_package = BitOperator.concatAll(data.getHeadBytes(),data.getBodyBytes());
        byte[] flow_id = BitOperator.splitBytes(item_package,11, 12);
        //根据第11个字节开始的流水号进行移除条件判断
		iResend.remove(new int[]{11},new byte[][]{flow_id});
	}
}
