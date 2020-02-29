package com.littlebayreal.easysocketlib.interfaces.connection;


import com.littlebayreal.easysocketlib.base.ConnectionInfo;

/**
 * Created by xuhao on 2017/6/30.
 * 连接通道切换
 */

public interface IConnectionSwitchListener {
    void onSwitchConnectionInfo(IConnectionManager manager, ConnectionInfo oldInfo, ConnectionInfo newInfo);
}
