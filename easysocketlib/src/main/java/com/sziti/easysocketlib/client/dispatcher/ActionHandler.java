package com.sziti.easysocketlib.client.dispatcher;

import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.base.EasySocketOptions;
import com.sziti.easysocketlib.client.delegate.action.SocketActionAdapter;
import com.sziti.easysocketlib.exceptions.ManuallyDisconnectException;
import com.sziti.easysocketlib.interfaces.action.ISocketActionListener;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.dispatcher.IRegister;

/**
 * Created by xuhao on 2017/5/18.
 */

public class ActionHandler extends SocketActionAdapter {
	private IConnectionManager mManager;

	private EasySocketOptions.IOThreadMode mCurrentThreadMode;

	private boolean iOThreadIsCalledDisconnect = false;

	public ActionHandler() {

	}

	/**
	 * 注册socket监听
	 *
	 * @param manager
	 * @param register
	 */
	public void attach(IConnectionManager manager, IRegister<ISocketActionListener, IConnectionManager> register) {
		this.mManager = manager;
		//实际调用的是AbsConnectionManager中的registerReceiver方法 actionDispatcher获得actionhandler
		register.registerReceiver(this);
	}

	public void detach(IRegister register) {
		register.unRegisterReceiver(this);
	}

	@Override
	public void onSocketIOThreadStart(String action) {
		if (mManager.getOption().getIOThreadMode() != mCurrentThreadMode) {
			mCurrentThreadMode = mManager.getOption().getIOThreadMode();
		}
		iOThreadIsCalledDisconnect = false;
	}

	@Override
	public void onSocketIOThreadShutdown(String action, Exception e) {
		if (mCurrentThreadMode != mManager.getOption().getIOThreadMode()) {//切换线程模式,不需要断开连接
			//do nothing
		} else {//多工模式
			if (!iOThreadIsCalledDisconnect) {//保证只调用一次,多工多线程,会调用两次
				iOThreadIsCalledDisconnect = true;
				if (!(e instanceof ManuallyDisconnectException)) {
					mManager.disconnect(e);
				}
			}
		}
	}

	@Override
	public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
		mManager.disconnect(e);
	}
}
