package com.littlebayreal.easysocketlib.client.delegate.connection;

import android.text.TextUtils;

import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.base.EasySocketOptions;
import com.littlebayreal.easysocketlib.base.OkSocketSSLConfig;
import com.littlebayreal.easysocketlib.client.PulseManager;
import com.littlebayreal.easysocketlib.client.ResendManager;
import com.littlebayreal.easysocketlib.client.delegate.action.AbsSocketResendHandler;
import com.littlebayreal.easysocketlib.client.dispatcher.ActionHandler;
import com.littlebayreal.easysocketlib.client.dispatcher.ActionResendDispatcher;
import com.littlebayreal.easysocketlib.exceptions.ManuallyDisconnectException;
import com.littlebayreal.easysocketlib.exceptions.UnConnectException;
import com.littlebayreal.easysocketlib.interfaces.action.IAction;
import com.littlebayreal.easysocketlib.interfaces.connection.IConnectionManager;
import com.littlebayreal.easysocketlib.interfaces.io.IIOManager;
import com.littlebayreal.easysocketlib.interfaces.send.ISendable;
import com.littlebayreal.easysocketlib.iothreads.ResendIOThreadManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Create by LiTtleBayReal
 * 增加了补传能力的连接管理器
 */
public class AutoResendConnectionManagerImpl extends AbsConnectionManager {
	/**
	 * 套接字
	 */
	private volatile Socket mSocket;
	/**
	 * Socket行为监听器
	 */
	private ActionHandler mActionHandler;
	/**
	 * 重发行为处理器
	 */
	private AbsSocketResendHandler mResendActionHandler;
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
	private volatile ResendManager mResendManager;
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
		//设置带有重发功能分发者
		setCustomDispatcher(new ActionResendDispatcher(remoteInfo,this));
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
		return isDisconnecting;
	}

	@Override
	public PulseManager getPulseManager() {
		return mPulseManager;
	}

	@Override
	public void setIsConnectionHolder(boolean isHold) {
		mOptions = new EasySocketOptions.Builder(mOptions).setConnectionHolden(isHold).build();
	}

	@Override
	public void setLocalConnectionInfo(ConnectionInfo localConnectionInfo) {
		if (isConnect()) {
			throw new IllegalStateException("Socket is connected, can't set local info after connect.");
		}
		mLocalConnectionInfo = localConnectionInfo;
	}

	@Override
	public AbsReconnectionManager getReconnectionManager() {
		return mOptions.getReconnectionManager();
	}

	public AbsSocketResendHandler getSocketResendHander(){
		return mOptions.getmSocketResendHandler();
	}
	@Override
	public IConnectionManager option(EasySocketOptions okOptions) {
		if (okOptions == null) {
			return this;
		}
		mOptions = okOptions;
		if (mManager != null) {
			mManager.setOkOptions(mOptions);
		}

		if (mPulseManager != null) {
			mPulseManager.setOkOptions(mOptions);
		}
		if (mReconnectionManager != null && !mReconnectionManager.equals(mOptions.getReconnectionManager())) {
			if (mReconnectionManager != null) {
				mReconnectionManager.detach();
			}
			SLog.i("reconnection manager is replaced");
			mReconnectionManager = mOptions.getReconnectionManager();
			mReconnectionManager.attach(this);
		}

		//初始化补发管理器以及补发事件监听处理器
		if (mResendManager == null)
			mResendManager = new ResendManager(this,mActionDispatcher);

		if (mResendActionHandler != null){
			mResendActionHandler.detach(this);
			SLog.i("mResendActionHandler is detached.");
		}
		if (mOptions.getmSocketResendHandler() == null)
			throw new RuntimeException("please set socketResendHandler first");
		mResendActionHandler = mOptions.getmSocketResendHandler();
		mResendActionHandler.attach(this,mResendManager,this);
		SLog.i("mResendActionHandler is attached.");
		return this;
	}

	@Override
	public EasySocketOptions getOption() {
		return mOptions;
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
				mSocket = getSocketByConfig();
				if (mLocalConnectionInfo != null) {
					SLog.i("try bind: " + mLocalConnectionInfo.getIp() + " port:" + mLocalConnectionInfo.getPort());
					mSocket.bind(new InetSocketAddress(mLocalConnectionInfo.getIp(), mLocalConnectionInfo.getPort()));
				}

				SLog.i("Start connect: " + mRemoteConnectionInfo.getIp() + ":" + mRemoteConnectionInfo.getPort() + " socket server...");
				mSocket.connect(new InetSocketAddress(mRemoteConnectionInfo.getIp(), mRemoteConnectionInfo.getPort()), mOptions.getConnectTimeoutSecond() * 1000);
				//关闭Nagle算法,无论TCP数据报大小,立即发送
				mSocket.setTcpNoDelay(false);
				mSocket.setSoLinger(true,0);
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
		//初始化心跳管理器(如果选择不打开心跳管理器 那么将不会创建)
	    if (mOptions.isOpenPulse())//(2020/8/21 0.1.0)
		mPulseManager = new PulseManager(this, mOptions);
		//初始化socket的读写进程
		mManager = new ResendIOThreadManager(
			getRemoteConnectionInfo(),
			mSocket.getInputStream(),
			mSocket.getOutputStream(),
			mOptions,
			mActionDispatcher);
		mManager.startEngine();
	}
	@Override
	public void disconnect(Exception exception) {
//		SLog.i("disconnect断开连接方法调用,本次错误信息:"+ exception.getMessage());
		synchronized (this) {
			if (isDisconnecting) {
				return;
			}
			isDisconnecting = true;

			if (mPulseManager != null) {
				mPulseManager.dead();
				mPulseManager = null;
			}
            //关闭补发功能
			if (mResendActionHandler != null){
				mResendActionHandler.dead();
			}
		}
        //正常断开 需要解除重连管理器
		if (exception instanceof ManuallyDisconnectException) {
			if (mReconnectionManager != null) {
				mReconnectionManager.detach();
				SLog.i("ReconnectionManager is detached.");
			}
		}
		//使用子线程进行socket通道的断连
		synchronized (this) {
			String info = mRemoteConnectionInfo.getIp() + ":" + mRemoteConnectionInfo.getPort();
			DisconnectThread thread = new DisconnectThread(exception, "Disconnect Thread for " + info);
			thread.setDaemon(true);
			thread.start();
		}
	}

	@Override
	public void disconnect() {
		disconnect(new ManuallyDisconnectException());
	}

	@Override
	public IConnectionManager send(ISendable sendable) {
		//在发送之前先放入补发线程
		mResendActionHandler.addForResend(sendable);
		if (mManager != null && sendable != null && isConnect()) {
			mManager.send(sendable);
		}
		return this;
	}

	/**
	 * 断开socket通道的线程
	 */
	private class DisconnectThread extends Thread {
		private Exception mException;

		public DisconnectThread(Exception exception, String name) {
			super(name);
			mException = exception;
		}

		@Override
		public void run() {
			try {
				if (mManager != null) {
					mManager.close(mException);
				}
				if (mConnectThread != null && mConnectThread.isAlive()) {
					mConnectThread.interrupt();
					try {
						SLog.i("disconnect thread need waiting for connection thread done.");
						mConnectThread.join();
					} catch (InterruptedException e) {
					}
					SLog.i("connection thread is done. disconnection thread going on");
					mConnectThread = null;
				}

				if (mSocket != null) {
					try {
						mSocket.close();
					} catch (IOException e) {
					}
				}
				if (mActionHandler != null) {
					mActionHandler.detach(AutoResendConnectionManagerImpl.this);
					SLog.i("mActionHandler is detached.");
					mActionHandler = null;
				}
			} finally {
				isDisconnecting = false;
				isConnectionPermitted = true;
				if (!(mException instanceof UnConnectException) && mSocket != null) {
					mException = mException instanceof ManuallyDisconnectException ? null : mException;
					sendBroadcast(IAction.ACTION_DISCONNECTION, mException);
				}
				mSocket = null;
				if (mException != null) {
					SLog.e("socket is disconnecting because: " + mException.getMessage());
					if (mOptions.isDebug()) {
						mException.printStackTrace();
					}
				}
			}
		}
	}
	private synchronized Socket getSocketByConfig() throws Exception {
		//自定义socket操作  暂时没用到
		if (mOptions.getOkSocketFactory() != null) {
			return mOptions.getOkSocketFactory().createSocket(mRemoteConnectionInfo, mOptions);
		}
		//默认操作
		OkSocketSSLConfig config = mOptions.getSSLConfig();
		if (config == null) {
			return new Socket();
		}
		SSLSocketFactory factory = config.getCustomSSLFactory();
		if (factory == null) {
			String protocol = "SSL";
			if (!TextUtils.isEmpty(config.getProtocol())) {
				protocol = config.getProtocol();
			}
			TrustManager[] trustManagers = config.getTrustManagers();
			if (trustManagers == null || trustManagers.length == 0) {
				//缺省信任所有证书
//                trustManagers = new TrustManager[]{new DefaultX509ProtocolTrustManager()};
			}
			try {
				SSLContext sslContext = SSLContext.getInstance(protocol);
				sslContext.init(config.getKeyManagers(), trustManagers, new SecureRandom());
				return sslContext.getSocketFactory().createSocket();
			} catch (Exception e) {
				if (mOptions.isDebug()) {
					e.printStackTrace();
				}
				SLog.e(e.getMessage());
				return new Socket();
			}
		} else {
			try {
				return factory.createSocket();
			} catch (IOException e) {
				if (mOptions.isDebug()) {
					e.printStackTrace();
				}
				SLog.e(e.getMessage());
				return new Socket();
			}
		}
	}
}
