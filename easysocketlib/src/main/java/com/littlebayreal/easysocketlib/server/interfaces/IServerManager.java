package com.littlebayreal.easysocketlib.server.interfaces;

import com.littlebayreal.easysocketlib.interfaces.io.IIOCoreOptions;

public interface IServerManager<E extends IIOCoreOptions> extends IServerShutdown {

    void listen();

    void listen(E options);

    boolean isLive();

    IClientPool<String, IClient> getClientPool();
}
