package com.sziti.easysocketlib.base;

import java.net.Socket;

public abstract class OkSocketFactory {
	public abstract Socket createSocket(ConnectionInfo info, EasySocketOptions options) throws Exception;
}
