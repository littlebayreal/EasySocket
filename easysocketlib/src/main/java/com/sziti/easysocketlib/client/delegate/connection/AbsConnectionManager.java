package com.sziti.easysocketlib.client.delegate.connection;


import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.client.dispatcher.ActionDispatcher;
import com.sziti.easysocketlib.interfaces.action.ISocketActionListener;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.connection.IConnectionSwitchListener;
import java.io.Serializable;


/**
 * Created by xuhao on 2017/5/17.
 */

public abstract class AbsConnectionManager implements IConnectionManager {
    /**
     * 连接信息
     */
    protected ConnectionInfo mRemoteConnectionInfo;
    /**
     * 本地绑定信息
     */
    protected ConnectionInfo mLocalConnectionInfo;
    /**
     * 连接信息switch监听器
     */
    private IConnectionSwitchListener mConnectionSwitchListener;
    /**
     * 状态机  监听分发器
     */
    protected ActionDispatcher mActionDispatcher;

    public AbsConnectionManager(ConnectionInfo info) {
        this(info, null);
    }

    public AbsConnectionManager(ConnectionInfo remoteInfo, ConnectionInfo localInfo) {
        mRemoteConnectionInfo = remoteInfo;
        mLocalConnectionInfo = localInfo;
        mActionDispatcher = new ActionDispatcher(remoteInfo, this);
    }

    public IConnectionManager registerReceiver(final ISocketActionListener socketResponseHandler) {
        mActionDispatcher.registerReceiver(socketResponseHandler);
        return this;
    }

    public IConnectionManager unRegisterReceiver(ISocketActionListener socketResponseHandler) {
        mActionDispatcher.unRegisterReceiver(socketResponseHandler);
        return this;
    }

    protected void sendBroadcast(String action, Serializable serializable) {
        mActionDispatcher.sendBroadcast(action, serializable);
    }

    protected void sendBroadcast(String action) {
        mActionDispatcher.sendBroadcast(action);
    }

    @Override
    public ConnectionInfo getRemoteConnectionInfo() {
        if (mRemoteConnectionInfo != null) {
            return mRemoteConnectionInfo.clone();
        }
        return null;
    }

    @Override
    public ConnectionInfo getLocalConnectionInfo() {
        if (mLocalConnectionInfo != null) {
            return mLocalConnectionInfo;
        }
        return null;
    }

	/**
	 * 根据新的连接信息切换连接
	 * @param info 新的连接信息
	 */
    @Override
    public synchronized void switchConnectionInfo(ConnectionInfo info) {
        if (info != null) {
            ConnectionInfo tempOldInfo = mRemoteConnectionInfo;
            mRemoteConnectionInfo = info.clone();
            if (mActionDispatcher != null) {
                mActionDispatcher.setConnectionInfo(mRemoteConnectionInfo);
            }
            //重连时将信息保存在managerholder中
            if (mConnectionSwitchListener != null) {
                mConnectionSwitchListener.onSwitchConnectionInfo(this, tempOldInfo, mRemoteConnectionInfo);
            }
        }
    }

    public void setOnConnectionSwitchListener(IConnectionSwitchListener listener) {
        mConnectionSwitchListener = listener;
    }
}
