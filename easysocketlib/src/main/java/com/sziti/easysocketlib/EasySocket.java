package com.sziti.easysocketlib;

import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.base.ManagerHolder;
import com.sziti.easysocketlib.base.EasySocketOptions;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.dispatcher.IRegister;
import com.sziti.easysocketlib.server.interfaces.IServerActionListener;
import com.sziti.easysocketlib.server.interfaces.IServerManager;

/**
 * EasySocket是一款轻量级的socket通讯框架
 */
public class EasySocket {
	//对连接对象的整体管理持有者 因为对于okSocket来说 连接的数量往往并不会只有一个
	private static ManagerHolder holder = ManagerHolder.getInstance();
	/**
	 * 获得一个SocketServer服务器.
	 *
	 * @param serverPort
	 * @return
	 */
	public static IRegister<IServerActionListener, IServerManager> server(int serverPort) {
		return (IRegister<IServerActionListener, IServerManager>) holder.getServer(serverPort);
	}
	/**
	 * 开启一个socket通讯通道,参配为默认参配
	 *
	 * @param connectInfo 连接信息{@link ConnectionInfo}
	 * @return 该参数的连接管理器 {@link IConnectionManager} 连接参数仅作为配置该通道的参配,不影响全局参配
	 */
	public static IConnectionManager open(ConnectionInfo connectInfo) {
		return holder.getConnection(connectInfo);
	}

	/**
	 * 开启一个socket通讯通道,参配为默认参配
	 *
	 * @param ip   需要连接的主机IPV4地址
	 * @param port 需要连接的主机开放的Socket端口号
	 * @return 该参数的连接管理器 {@link IConnectionManager} 连接参数仅作为配置该通道的参配,不影响全局参配
	 */
	public static IConnectionManager open(String ip, int port) {
		ConnectionInfo info = new ConnectionInfo(ip, port);
		return holder.getConnection(info);
	}

	/**
	 * 开启一个socket通讯通道
	 * Deprecated please use {@link EasySocket#open(ConnectionInfo)}@{@link IConnectionManager#option(EasySocketOptions)}
	 *
	 * @param connectInfo 连接信息{@link ConnectionInfo}
	 * @param okOptions   连接参配{@link EasySocketOptions}
	 * @return 该参数的连接管理器 {@link IConnectionManager} 连接参数仅作为配置该通道的参配,不影响全局参配
	 * @deprecated
	 */
	public static IConnectionManager open(ConnectionInfo connectInfo, EasySocketOptions okOptions) {
		return holder.getConnection(connectInfo, okOptions);
	}
	/**
	 * 开启一个socket通讯通道
	 * Deprecated please use {@link EasySocket#open(String, int)}@{@link IConnectionManager#option(EasySocketOptions)}
	 *
	 * @param ip        需要连接的主机IPV4地址
	 * @param port      需要连接的主机开放的Socket端口号
	 * @param okOptions 连接参配{@link EasySocketOptions}
	 * @return 该参数的连接管理器 {@link IConnectionManager}
	 * @deprecated
	 */
	public static IConnectionManager open(String ip, int port, EasySocketOptions okOptions) {
		ConnectionInfo info = new ConnectionInfo(ip, port);
		return holder.getConnection(info, okOptions);
	}
}
