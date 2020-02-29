package com.littlebayreal.easysocketlib.interfaces.io;

/**
 * Created by xuhao on 2017/5/17.
 */

public interface IStateSender<T> {

    void sendBroadcast(String action, T serializable);

    void sendBroadcast(String action);
}
