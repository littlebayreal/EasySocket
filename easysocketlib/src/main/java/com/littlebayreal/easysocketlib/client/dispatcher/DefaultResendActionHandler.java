package com.littlebayreal.easysocketlib.client.dispatcher;

import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.client.delegate.action.AbsSocketResendHandler;
import com.littlebayreal.easysocketlib.client.pojo.BaseSendData;
import com.littlebayreal.easysocketlib.client.pojo.OriginalData;
import com.littlebayreal.easysocketlib.interfaces.action.IRendRemove;
import com.littlebayreal.easysocketlib.interfaces.send.IResend;
import com.littlebayreal.easysocketlib.util.BitOperator;
import com.littlebayreal.easysocketlib.util.HexStringUtils;

import java.util.List;

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
		SLog.e("IResend对象:" + iResend.toString());

		//开始补发操作
		iResend.startEngine();
	}

	@Override
	public void setRemovePolicy() {
		//noting 用户自己设置移除策略
		//用户设置补发的移除逻辑
		iResend.setRemoveCallBack(new IRendRemove<BaseSendData>() {
			@Override
			public void remove(OriginalData originalData, List<BaseSendData> resendList) {
				byte[] item_package = BitOperator.concatAll(originalData.getHeadBytes(), originalData.getBodyBytes());
				SLog.i("DefaultResendActionHandler:移除补发对象:"+ HexStringUtils.toHexString(item_package));
				byte[] flow_id = BitOperator.splitBytes(item_package, 11, 12);
				for (int k = 0; k < resendList.size(); k++) {
					byte[] temp = resendList.get(k).parse();
					byte[] temp_check = BitOperator.splitBytes(temp,11,12);
					//满足条件  直接移除
					if (BitOperator.byteToInteger(flow_id) == BitOperator.byteToInteger(temp_check)){
						SLog.i("DefaultResendActionHandler:满足条件，移除对象:"+ HexStringUtils.toHexString(item_package));
						resendList.remove(k);
					}
				}
			}
		});
	}

	//可做补发数据的添加操作
	@Override
	public void onSocketWriteFailed(String action, BaseSendData data) {
		//发送失败的数据也要放入补发队列
		iResend.add(data);
	}

	//可做补发数据的移除操作
	@Override
	public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
		//根据设置的条件进行已发送数据的补发移除
//        byte[] item_package = BitOperator.concatAll(data.getHeadBytes(),data.getBodyBytes());
//        byte[] flow_id = BitOperator.splitBytes(item_package,11, 12);
//        //根据第11个字节开始的流水号进行移除条件判断
//		iResend.remove(new int[]{11},new byte[][]{flow_id});
		iResend.remove(data);
	}
}
