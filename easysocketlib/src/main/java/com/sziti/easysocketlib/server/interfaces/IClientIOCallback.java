package com.sziti.easysocketlib.server.interfaces;


import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.interfaces.send.ISendable;

public interface IClientIOCallback {

    void onClientRead(OriginalData originalData, IClient client, IClientPool<IClient, String> clientPool);

    void onClientWrite(ISendable sendable, IClient client, IClientPool<IClient, String> clientPool);

}
