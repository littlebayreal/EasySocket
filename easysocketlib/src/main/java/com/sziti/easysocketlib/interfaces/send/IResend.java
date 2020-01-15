package com.sziti.easysocketlib.interfaces.send;

public interface IResend<BaseSendData> {
	//添加需要补传的数据
	void add(BaseSendData data);
	//移除补传的数据
	void remove(BaseSendData data);
	//停止重发
	void dead();
}
