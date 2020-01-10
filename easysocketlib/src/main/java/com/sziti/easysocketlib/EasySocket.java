package com.sziti.easysocketlib;

/**
 * EasySocket是一款轻量级的socket通讯框架
 */
public class EasySocket {
	//对连接对象的整体管理持有者 因为对于okSocket来说 连接的数量往往并不会只有一个
	private static ManagerHolder holder = ManagerHolder.getInstance();
}
