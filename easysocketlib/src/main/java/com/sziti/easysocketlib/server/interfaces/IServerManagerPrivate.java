package com.sziti.easysocketlib.server.interfaces;


import com.sziti.easysocketlib.interfaces.io.IIOCoreOptions;

public interface IServerManagerPrivate<E extends IIOCoreOptions> extends IServerManager<E> {
    void initServerPrivate(int serverPort);
}
