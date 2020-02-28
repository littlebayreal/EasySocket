package com.sziti.easysocketlib.server.action;
import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.interfaces.action.IOAction;
import com.sziti.easysocketlib.interfaces.io.IStateSender;
import com.sziti.easysocketlib.interfaces.send.ISendable;

import java.io.Serializable;

import static com.sziti.easysocketlib.server.action.IAction.Client.ACTION_READ_THREAD_SHUTDOWN;
import static com.sziti.easysocketlib.server.action.IAction.Client.ACTION_READ_THREAD_START;
import static com.sziti.easysocketlib.server.action.IAction.Client.ACTION_WRITE_THREAD_SHUTDOWN;
import static com.sziti.easysocketlib.server.action.IAction.Client.ACTION_WRITE_THREAD_START;

public class ClientActionDispatcher implements IStateSender<Serializable> {

    private ClientActionListener mActionListener;

    public ClientActionDispatcher(ClientActionListener actionListener) {
        mActionListener = actionListener;
    }

    @Override
    public void sendBroadcast(final String action, final Serializable serializable) {
        if (mActionListener == null) {
            return;
        }
        //分发从io线程中传回的消息
        dispatch(action, serializable);
    }

    @Override
    public void sendBroadcast(String action) {
        sendBroadcast(action, null);
    }

    private void dispatch(String action, Serializable serializable) {
        switch (action) {
            case ACTION_READ_THREAD_START: {
                try {
                    mActionListener.onClientReadReady();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case ACTION_READ_THREAD_SHUTDOWN: {
                try {
                    Exception exception = (Exception) serializable;
                    mActionListener.onClientReadDead(exception);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case ACTION_WRITE_THREAD_START: {
                try {
                    mActionListener.onClientWriteReady();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case ACTION_WRITE_THREAD_SHUTDOWN: {
                try {
                    Exception exception = (Exception) serializable;
                    mActionListener.onClientWriteDead(exception);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case IOAction.ACTION_READ_COMPLETE: {
                try {
                    OriginalData data = (OriginalData) serializable;
                    mActionListener.onClientRead(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case IOAction.ACTION_WRITE_COMPLETE: {
                try {
                    ISendable data = (ISendable) serializable;
                    mActionListener.onClientWrite(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public interface ClientActionListener {
        void onClientReadReady();

        void onClientWriteReady();

        void onClientReadDead(Exception e);

        void onClientWriteDead(Exception e);

        void onClientRead(OriginalData originalData);

        void onClientWrite(ISendable sendable);
    }
}
