package com.littlebayreal.easysocketlib.client.delegate.connection;


import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.base.AbsLoopThread;
import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.exceptions.ManuallyDisconnectException;

import java.util.Iterator;

/**
 * Created by xuhao on 2017/6/5.
 * 默认的重连管理器
 * 事件驱动
 */

public class DefaultReconnectManager extends AbsReconnectionManager {

	/**
	 * 最大连接失败次数,不包括断开异常
	 */
	private static final int MAX_CONNECTION_FAILED_TIMES = 5;
	/**
	 * 连接失败次数,不包括断开异常
	 */
	private int mConnectionFailedTimes = 0;

	private volatile ReconnectTestingThread mReconnectTestingThread;

	public DefaultReconnectManager() {
		mReconnectTestingThread = new ReconnectTestingThread();
	}

	//断开连接后的逻辑处理
	@Override
	public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
		if (isNeedReconnect(e)) {//break with exception
			reconnectDelay();
		} else {
			resetThread();
		}
	}

	@Override
	public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
		resetThread();
	}

	//连接失败后的逻辑处理
	@Override
	public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
		if (e != null) {
			mConnectionFailedTimes++;
			if (mConnectionFailedTimes > MAX_CONNECTION_FAILED_TIMES) {
				resetThread();
				//连接失败达到阈值,需要切换备用线路.
				ConnectionInfo originInfo = mConnectionManager.getRemoteConnectionInfo();
				ConnectionInfo backupInfo = originInfo.getBackupInfo();
				if (backupInfo != null) {
					ConnectionInfo bbInfo = new ConnectionInfo(originInfo.getIp(), originInfo.getPort());
					backupInfo.setBackupInfo(bbInfo);
					if (!mConnectionManager.isConnect()) {
						SLog.i("Prepare switch to the backup line " + backupInfo.getIp() + ":" + backupInfo.getPort() + " ...");
						synchronized (mConnectionManager) {
							mConnectionManager.switchConnectionInfo(backupInfo);
						}
						reconnectDelay();
					}
				} else {
					reconnectDelay();
				}
			} else {
				reconnectDelay();
			}
		}
	}

	/**
	 * 是否需要重连
	 *
	 * @param e
	 * @return
	 */
	private boolean isNeedReconnect(Exception e) {
		synchronized (mIgnoreDisconnectExceptionList) {
			if (e != null && !(e instanceof ManuallyDisconnectException)) {//break with exception
				Iterator<Class<? extends Exception>> it = mIgnoreDisconnectExceptionList.iterator();
				while (it.hasNext()) {
					Class<? extends Exception> classException = it.next();
					if (classException.isAssignableFrom(e.getClass())) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
	}


	/**
	 * 重置重连线程,关闭线程
	 */
	private synchronized void resetThread() {
		if (mReconnectTestingThread != null) {
			mReconnectTestingThread.shutdown();
			mReconnectTestingThread = null;
		}
	}

	/**
	 * 开始延迟重连
	 */
	private void reconnectDelay() {
		if (mReconnectTestingThread == null) {
			mReconnectTestingThread = new ReconnectTestingThread();
			mReconnectTestingThread.start();
		} else {
			synchronized (mReconnectTestingThread) {
				if (mReconnectTestingThread.isShutdown()) {
					mReconnectTestingThread.start();
				}
			}
		}
	}

	@Override
	public void detach() {
		super.detach();
	}

	//尝试连接线程
	private class ReconnectTestingThread extends AbsLoopThread {
		/**
		 * 延时连接时间
		 */
		private long mReconnectTimeDelay = 10 * 1000;

		@Override

		protected void beforeLoop() throws Exception {
			super.beforeLoop();
			//连接尝试时间需要根据次数延长
			if (mReconnectTimeDelay < mConnectionManager.getOption().getConnectTimeoutSecond() * 1000) {
				mReconnectTimeDelay = mConnectionManager.getOption().getConnectTimeoutSecond() * 1000;
			}
		}

		@Override
		protected void runInLoopThread() throws Exception {
			if (mDetach) {
				SLog.i("ReconnectionManager already detached by framework.We decide gave up this reconnection mission!");
				shutdown();
				return;
			}

			//延迟执行
			SLog.i("Reconnect after " + mReconnectTimeDelay + " mills ...");
			Thread.sleep(mReconnectTimeDelay);
			if (mDetach) {
				SLog.i("ReconnectionManager already detached by framework.We decide gave up this reconnection mission!");
				shutdown();
				return;
			}
			if (mConnectionManager.isConnect()) {
				shutdown();
				return;
			}
			boolean isHolden = mConnectionManager.getOption().isConnectionHolden();
			//如果是单次使用的连接 那么就不需要重连
			if (!isHolden) {
				detach();
				shutdown();
				return;
			}
			ConnectionInfo info = mConnectionManager.getRemoteConnectionInfo();
			SLog.i("Reconnect the server " + info.getIp() + ":" + info.getPort() + " ...");
			synchronized (mConnectionManager) {
				//正式尝试重连
				if (!mConnectionManager.isConnect()) {
					mConnectionManager.connect();
				} else {
					shutdown();
				}
			}
		}
		@Override
		protected void loopFinish(Exception e) {
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		return true;
	}

}
