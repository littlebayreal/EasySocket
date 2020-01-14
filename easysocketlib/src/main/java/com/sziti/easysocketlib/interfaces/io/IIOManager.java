package com.sziti.easysocketlib.interfaces.io;


import com.sziti.easysocketlib.interfaces.send.ISendable;

/**
 * Created by xuhao on 2017/5/16.
 */

public interface IIOManager<E extends IIOCoreOptions> {
    void startEngine();

    void setOkOptions(E options);

    void send(ISendable sendable);

    void close();

    void close(Exception e);

}
