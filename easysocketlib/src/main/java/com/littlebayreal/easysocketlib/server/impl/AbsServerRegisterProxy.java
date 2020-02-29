package com.littlebayreal.easysocketlib.server.impl;


import com.littlebayreal.easysocketlib.interfaces.dispatcher.IRegister;
import com.littlebayreal.easysocketlib.interfaces.io.IStateSender;
import com.littlebayreal.easysocketlib.server.action.ServerActionDispatcher;
import com.littlebayreal.easysocketlib.server.interfaces.IServerActionListener;
import com.littlebayreal.easysocketlib.server.interfaces.IServerManager;

import java.io.Serializable;

public class AbsServerRegisterProxy implements IRegister<IServerActionListener, IServerManager>, IStateSender<Serializable> {

    protected ServerActionDispatcher mServerActionDispatcher;

    private IServerManager<EasyServerOptions> mManager;

    protected void init(IServerManager<EasyServerOptions> serverManager) {
        mManager = serverManager;
        mServerActionDispatcher = new ServerActionDispatcher(mManager);
    }

    @Override
    public IServerManager<EasyServerOptions> registerReceiver(IServerActionListener socketActionListener) {
        return mServerActionDispatcher.registerReceiver(socketActionListener);
    }

    @Override
    public IServerManager<EasyServerOptions> unRegisterReceiver(IServerActionListener socketActionListener) {
        return mServerActionDispatcher.unRegisterReceiver(socketActionListener);
    }

    @Override
    public void sendBroadcast(String action, Serializable serializable) {
        mServerActionDispatcher.sendBroadcast(action, serializable);
    }

    @Override
    public void sendBroadcast(String action) {
        mServerActionDispatcher.sendBroadcast(action);
    }
}
