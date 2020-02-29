package com.littlebayreal.easysocketlib.server.action;

import com.littlebayreal.easysocketlib.server.interfaces.IClient;
import com.littlebayreal.easysocketlib.server.interfaces.IClientPool;
import com.littlebayreal.easysocketlib.server.interfaces.IServerActionListener;
import com.littlebayreal.easysocketlib.server.interfaces.IServerShutdown;

public abstract class ServerActionAdapter implements IServerActionListener {
    @Override
    public void onServerListening(int serverPort) {

    }

    @Override
    public void onClientConnected(IClient client, int serverPort, IClientPool clientPool) {

    }

    @Override
    public void onClientDisconnected(IClient client, int serverPort, IClientPool clientPool) {

    }

    @Override
    public void onServerWillBeShutdown(int serverPort, IServerShutdown shutdown, IClientPool clientPool, Throwable throwable) {

    }

    @Override
    public void onServerAlreadyShutdown(int serverPort) {

    }
}
