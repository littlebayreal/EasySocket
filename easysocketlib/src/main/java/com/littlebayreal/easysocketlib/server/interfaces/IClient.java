package com.littlebayreal.easysocketlib.server.interfaces;

import com.littlebayreal.easysocketlib.interfaces.connection.IDisConnectable;
import com.littlebayreal.easysocketlib.interfaces.dispatcher.IRegister;
import com.littlebayreal.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.littlebayreal.easysocketlib.interfaces.send.ISender;

import java.io.Serializable;

public interface IClient extends IDisConnectable, ISender<IClient>, Serializable, IRegister<IClientIOCallback,IClient> {

    String getHostIp();

    String getHostName();

    String getUniqueTag();

    void setReaderProtocol(IReaderProtocol protocol);

//    void addIOCallback(IClientIOCallback clientIOCallback);

//    void removeIOCallback(IClientIOCallback clientIOCallback);

    void removeAllIOCallback();

}
