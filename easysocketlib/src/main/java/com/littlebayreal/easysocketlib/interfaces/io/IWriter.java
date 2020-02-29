package com.littlebayreal.easysocketlib.interfaces.io;


import com.littlebayreal.easysocketlib.interfaces.send.ISendable;

import java.io.OutputStream;

/**
 * Created by xuhao on 2017/5/16.
 */

public interface IWriter<T extends IIOCoreOptions> {

    void initialize(OutputStream outputStream, IStateSender stateSender);

    boolean write() throws RuntimeException;

    void setOption(T option);

    void offer(ISendable sendable);

    void close();

}
