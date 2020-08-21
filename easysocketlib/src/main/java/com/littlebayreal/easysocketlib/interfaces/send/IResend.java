package com.littlebayreal.easysocketlib.interfaces.send;

import com.littlebayreal.easysocketlib.client.pojo.OriginalData;
import com.littlebayreal.easysocketlib.interfaces.action.IRendRemove;

public interface IResend<BaseSendData> {
	//添加需要补传的数据
	void add(BaseSendData data);
	/**
	 * 移除符合条件的补发消息
	 * @param  indices 在补发报文中对应的开始位置集合
	 * @param checkBytes 用于对比的报文内容
	 */
	void remove(int[] indices,byte[]... checkBytes);

	/**
	 * 移除补发消息
	 */
	void remove(OriginalData originalData);

	void setRemoveCallBack(IRendRemove callBack);
	//停止重发
	void dead();
	//开始重发任务线程
	void startEngine();
}
