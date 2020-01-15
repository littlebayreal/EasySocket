package com.sziti.easysocketlib.client.delegate.connection;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.base.EasySocketOptions;
import com.sziti.easysocketlib.client.PulseManager;
import com.sziti.easysocketlib.client.ResendManager;
import com.sziti.easysocketlib.client.dispatcher.ActionHandler;
import com.sziti.easysocketlib.exceptions.UnConnectException;
import com.sziti.easysocketlib.interfaces.action.IAction;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.io.IIOManager;
import com.sziti.easysocketlib.interfaces.send.ISendable;
import com.sziti.easysocketlib.iothreads.IOThreadManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Create by LiTtleBayReal
 * 增加了补传能力的连接管理器
 */
public class AutoResendConnectionManagerImpl extends AbsConnectionManager{
	/**
	 * 套接字
	 */
	private volatile Socket mSocket;
	/**
	 * Socket行为监听器
	 */
	private ActionHandler mActionHandler;
	/**
	 * 连接线程
	 */
	private Thread mConnectThread;
	/**
	 * 脉搏管理器
	 */
	private volatile PulseManager mPulseManager;
	/**
	 * 补发管理器
	 */
	private volatile ResendManager mResndManager;
	/**
	 * IO通讯管理器  它才是实际上的收发数据的管理器
	 */
	private IIOManager mManager;
	/**
	 * socket参配项
	 */
	private volatile EasySocketOptions mOptions;
	/**
	 * 能否连接
	 */
	private volatile boolean isConnectionPermitted = true;
	/**
	 * 重新连接管理器
	 */
	private volatile AbsReconnectionManager mReconnectionManager;

	/**
	 * 是否正在断开
	 */
	private volatile boolean isDisconnecting = false;
	public AutoResendConnectionManagerImpl(ConnectionInfo info) {
		this(info, null);
	}

	public AutoResendConnectionManagerImpl(ConnectionInfo remoteInfo, ConnectionInfo localInfo) {
		super(remoteInfo, localInfo);
		String ip = "";
		String port = "";
		if (remoteInfo != null) {
			ip = remoteInfo.getIp();
			port = remoteInfo.getPort() + "";
		}
		SLog.i("block connection init with:" + ip + ":" + port);

		if (localInfo != null) {
			SLog.i("binding local addr:" + localInfo.getIp() + " port:" + localInfo.getPort());
		}
	}

	@Override
	public boolean isConnect() {
		if (mSocket == null) {
			return false;
		}
		return mSocket.isConnected() && !mSocket.isClosed();
	}

	@Override
	public boolean isDisconnecting() {
		return false;
	}

	@Override
	public PulseManager getPulseManager() {
		return null;
	}

	@Override
	public void setIsConnectionHolder(boolean isHold) {

	}

	@Override
	public void setLocalConnectionInfo(ConnectionInfo localConnectionInfo) {

	}

	@Override
	public AbsReconnectionManager getReconnectionManager() {
		return null;
	}

	@Override
	public IConnectionManager option(EasySocketOptions okOptions) {
		return null;
	}

	@Override
	public EasySocketOptions getOption() {
		return null;
	}

	@Override
	public synchronized void connect() {
		SLog.i("Thread name:" + Thread.currentThread().getName() + " id:" + Thread.currentThread().getId());
		if (!isConnectionPermitted) {
			return;
		}
		//保证只能有一个socket对象在连接
		isConnectionPermitted = false;
		if (isConnect()) {
			return;
		}
		isDisconnecting = false;
		if (mRemoteConnectionInfo == null) {
			isConnectionPermitted = true;
			throw new UnConnectException("连接参数为空,检查连接参数");
		}
		//将上一次的socket连接状况的回调监听注销
		if (mActionHandler != null) {
			mActionHandler.detach(this);
			SLog.i("mActionHandler is detached.");
		}
		//重新注册socket的回调监听  这里面是对socket的一些情况的特殊处理  与用户的监听事件是分开的
		mActionHandler = new ActionHandler();
		//让ActionDispatcher绑定回调监听类  能够使socket的消息分发出来
		mActionHandler.attach(this, this);
		SLog.i("mActionHandler is attached.");
		//重连管理器重新生成
		if (mReconnectionManager != null) {
			mReconnectionManager.detach();
			SLog.i("ReconnectionManager is detached.");
		}
		//设置重连管理
		mReconnectionManager = mOptions.getReconnectionManager();
		if (mReconnectionManager != null) {
			mReconnectionManager.attach(this);
			SLog.i("ReconnectionManager is attached.");
		}

		String info = mRemoteConnectionInfo.getIp() + ":" + mRemoteConnectionInfo.getPort();
		mConnectThread = new ConnectionThread(" Connect thread for " + info);
		//设置连接线程为守护线程
		mConnectThread.setDaemon(true);
		mConnectThread.start();
	}
	private class ConnectionThread extends Thread {
		public ConnectionThread(String name) {
			super(name);
		}

		@Override
		public void run() {
			try {
				if (mLocalConnectionInfo != null) {
					SLog.i("try bind: " + mLocalConnectionInfo.getIp() + " port:" + mLocalConnectionInfo.getPort());
					mSocket.bind(new InetSocketAddress(mLocalConnectionInfo.getIp(), mLocalConnectionInfo.getPort()));
				}

				SLog.i("Start connect: " + mRemoteConnectionInfo.getIp() + ":" + mRemoteConnectionInfo.getPort() + " socket server...");
				mSocket.connect(new InetSocketAddress(mRemoteConnectionInfo.getIp(), mRemoteConnectionInfo.getPort()), mOptions.getConnectTimeoutSecond() * 1000);
				//关闭Nagle算法,无论TCP数据报大小,立即发送
				mSocket.setTcpNoDelay(true);
				resolveManager();
				sendBroadcast(IAction.ACTION_CONNECTION_SUCCESS);
				SLog.i("Socket server: " + mRemoteConnectionInfo.getIp() + ":" + mRemoteConnectionInfo.getPort() + " connect successful!");
			} catch (Exception e) {
				if (mOptions.isDebug()) {
					e.printStackTrace();
				}
				Exception exception = new UnConnectException(e);
				SLog.e("Socket server " + mRemoteConnectionInfo.getIp() + ":" + mRemoteConnectionInfo.getPort() + " connect failed! error msg:" + e.getMessage());
				sendBroadcast(IAction.ACTION_CONNECTION_FAILED, exception);
			} finally {
				isConnectionPermitted = true;
			}
		}
	}
	/**
	 * 将socket的输入输出流通过配置，分发器和输入输出流管理器绑定
	 * @throws IOException
	 */
	private void resolveManager() throws IOException {
		//初始化心跳管理器
		mPulseManager = new PulseManager(this, mOptions);
		//初始化补发管理器
        mResndManager = new ResendManager(this,mActionDispatcher);
		//初始化socket的读写进程
		mManager = new IOThreadManager(
			mSocket.getInputStream(),
			mSocket.getOutputStream(),
			mOptions,
			mActionDispatcher);
		mManager.startEngine();
	}
	@Override
	public void disconnect(Exception e) {

	}

	@Override
	public void disconnect() {

	}

	@Override
	public IConnectionManager send(ISendable sendable) {
		return null;
	}
}
