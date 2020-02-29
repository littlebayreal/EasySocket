package com.littlebayreal.easysocketlib.server.interfaces;

import com.littlebayreal.easysocketlib.interfaces.send.ISendable;

public interface IClientPool<T, K> {

    void cache(T t);

    T findByUniqueTag(K key);

    int size();

    void sendToAll(ISendable sendable);
}
