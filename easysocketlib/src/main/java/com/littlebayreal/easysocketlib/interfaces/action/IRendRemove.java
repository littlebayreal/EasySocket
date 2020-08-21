package com.littlebayreal.easysocketlib.interfaces.action;

import com.littlebayreal.easysocketlib.client.pojo.OriginalData;

import java.util.List;

public interface IRendRemove<BaseSendData> {
	/**
	 * 交给用户自己的策略删除相应的补发消息(同步方法)
	 * @param originalData 接收到的原始数据
	 * @param resendList 当前补发队列中的所有补发数据
	 */
	void remove(OriginalData originalData, List<BaseSendData> resendList);
}
