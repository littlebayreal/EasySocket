package com.sziti.easysocketlib.server.impl.clientpojo;

import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.interfaces.io.IStateSender;
import com.sziti.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.sziti.easysocketlib.interfaces.send.ISendable;
import com.sziti.easysocketlib.server.action.ClientActionDispatcher;
import com.sziti.easysocketlib.server.action.IAction;
import com.sziti.easysocketlib.server.exceptions.CacheException;
import com.sziti.easysocketlib.server.impl.EasyServerOptions;
import com.sziti.easysocketlib.server.impl.iocore.ClientIOManager;
import com.sziti.easysocketlib.server.interfaces.IClient;
import com.sziti.easysocketlib.server.interfaces.IClientIOCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 与客户端联通后的通道
 */
public class ClientImpl extends AbsClient {

    private volatile boolean isDead;

    private ClientIOManager mIOManager;

    private IStateSender mActionDispatcher;

    private volatile ClientPoolImpl mClientPool;

    private IStateSender mServerStateSender;

    private volatile boolean isReadThreadStarted;
    //对客户端设置的回调方法集合
    private volatile List<IClientIOCallback> mCallbackList = new ArrayList<>();

    public ClientImpl(Socket socket,
                      EasyServerOptions okServerOptions) {
        super(socket, okServerOptions);
        //创建客户端的事件监听器
        mActionDispatcher = new ClientActionDispatcher(this);
        try {
            initIOManager();
        } catch (IOException e) {
            disconnect(e);
        }
    }

    public void setClientPool(ClientPoolImpl clientPool) {
        mClientPool = clientPool;
    }

    public void setServerStateSender(IStateSender serverStateSender) {
        mServerStateSender = serverStateSender;
    }
    //初始化io收发线程
    private void initIOManager() throws IOException {
        InputStream inputStream = mSocket.getInputStream();
        OutputStream outputStream = mSocket.getOutputStream();
        mIOManager = new ClientIOManager(inputStream, outputStream, mOkServerOptions, mActionDispatcher);
    }

    public void startIOEngine() {
        if (mIOManager != null) {
            synchronized (mIOManager) {
                mIOManager.startWriteEngine();
            }
        }
    }

    @Override
    public void disconnect(Exception e) {
        if (mIOManager != null) {
            synchronized (mIOManager) {
                mIOManager.close(e);
            }
        } else {
            onClientDead(e);
        }
        try {
            synchronized (mSocket) {
                mSocket.close();
            }
        } catch (IOException e1) {
        }
        removeAllIOCallback();
        isReadThreadStarted = false;
    }

    @Override
    public void disconnect() {
        if (mIOManager != null) {
            synchronized (mIOManager) {
                mIOManager.close();
            }
        } else {
            onClientDead(null);
        }
        try {
            synchronized (mSocket) {
                mSocket.close();
            }
        } catch (IOException e1) {
        }
        removeAllIOCallback();
        isReadThreadStarted = false;
    }

    @Override
    public IClient send(ISendable sendable) {
        if (mIOManager != null) {
            mIOManager.send(sendable);
        }
        return this;
    }

    @Override
    protected void onClientReady() {
        if (isDead) {
            return;
        }
        mClientPool.cache(this);
        mServerStateSender.sendBroadcast(IAction.Server.ACTION_CLIENT_CONNECTED, this);
    }

    @Override
    protected void onClientDead(Exception e) {
        if (isDead) {
            return;
        }
        if (!(e instanceof CacheException)) {
            mClientPool.unCache(this);
        }
        if (e != null) {
            if (mOkServerOptions.isDebug()) {
                e.printStackTrace();
            }
        }
        disconnect(e);
        mServerStateSender.sendBroadcast(IAction.Server.ACTION_CLIENT_DISCONNECTED, this);
        synchronized (this) {
            isDead = true;
        }
    }

    @Override
    public void setReaderProtocol(IReaderProtocol protocol) {
        if (mIOManager != null) {
            synchronized (mIOManager) {
                EasyServerOptions.Builder builder = new EasyServerOptions.Builder(mOkServerOptions);
                builder.setReaderProtocol(protocol);
                mOkServerOptions = builder.build();
                mIOManager.setOkOptions(mOkServerOptions);
            }
        }
    }

//    @Override
//    public void addIOCallback(IClientIOCallback clientIOCallback) {
//        if (isDead) {
//            return;
//        }
//        synchronized (mCallbackList) {
//            mCallbackList.add(clientIOCallback);
//        }
//        synchronized (mIOManager) {
//            if (!isReadThreadStarted) {
//                isReadThreadStarted = true;
//                mIOManager.startReadEngine();
//            }
//        }
//    }

//    @Override
//    public void removeIOCallback(IClientIOCallback clientIOCallback) {
//        synchronized (mCallbackList) {
//            mCallbackList.remove(clientIOCallback);
//        }
//    }

    @Override
    public void removeAllIOCallback() {
        synchronized (mCallbackList) {
            mCallbackList.clear();
        }
    }

    @Override
    public void onClientRead(OriginalData originalData) {
        List<IClientIOCallback> list = new ArrayList<>();
        list.addAll(mCallbackList);

        for (IClientIOCallback clientIOCallback : list) {
            try {
                clientIOCallback.onClientRead(originalData, this, mClientPool);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClientWrite(ISendable sendable) {
        List<IClientIOCallback> list = new ArrayList<>();
        list.addAll(mCallbackList);

        for (IClientIOCallback clientIOCallback : list) {
            try {
                clientIOCallback.onClientWrite(sendable, this, mClientPool);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	@Override
	public IClient registerReceiver(IClientIOCallback clientIOCallback) {

		if (isDead) {
            return this;
        }
        synchronized (mCallbackList) {
            mCallbackList.add(clientIOCallback);
        }
        synchronized (mIOManager) {
            if (!isReadThreadStarted) {
                isReadThreadStarted = true;
                mIOManager.startReadEngine();
            }
        }
		return this;
	}

	@Override
	public IClient unRegisterReceiver(IClientIOCallback clientIOCallback) {
		 synchronized (mCallbackList) {
            mCallbackList.remove(clientIOCallback);
        }
        return this;
	}
}
