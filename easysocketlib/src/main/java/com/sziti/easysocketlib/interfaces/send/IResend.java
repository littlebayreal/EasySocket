package com.sziti.easysocketlib.interfaces.send;

public interface IResend<Serializable> {
	//添加需要补传的数据
	void add(Serializable data);
	//移除补传的数据
	void remove(Serializable data);
	//停止重发
	void dead();
	//开始重发任务线程
	void startEngine();
}
