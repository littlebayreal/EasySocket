package com.littlebayreal.easysocketlib.server.interfaces;


import com.littlebayreal.easysocketlib.interfaces.io.IIOCoreOptions;

public interface IServerManagerPrivate<E extends IIOCoreOptions> extends IServerManager<E> {
    void initServerPrivate(int serverPort);
}
