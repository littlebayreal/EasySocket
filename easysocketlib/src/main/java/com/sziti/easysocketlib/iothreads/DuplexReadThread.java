package com.sziti.easysocketlib.iothreads;


import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.base.AbsLoopThread;
import com.sziti.easysocketlib.exceptions.ManuallyDisconnectException;
import com.sziti.easysocketlib.interfaces.action.IAction;
import com.sziti.easysocketlib.interfaces.io.IReader;
import com.sziti.easysocketlib.interfaces.io.IStateSender;

import java.io.IOException;

/**
 * Created by xuhao on 2017/5/17.
 */

public class DuplexReadThread extends AbsLoopThread {
    private IStateSender mStateSender;

    private IReader mReader;

    public DuplexReadThread(IReader reader, IStateSender stateSender) {
        super("client_duplex_read_thread");
        this.mStateSender = stateSender;
        this.mReader = reader;
    }

    @Override
    protected void beforeLoop() {
        mStateSender.sendBroadcast(IAction.ACTION_READ_THREAD_START);
    }

    //在工作线程中不停的读取buffer
    @Override
    protected void runInLoopThread() throws IOException {
        mReader.read();
    }

    @Override
    public synchronized void shutdown(Exception e) {
        mReader.close();
        super.shutdown(e);
    }

    @Override
    protected void loopFinish(Exception e) {
        e = e instanceof ManuallyDisconnectException ? null : e;
        if (e != null) {
            SLog.e("duplex read error,thread is dead with exception:" + e.getMessage());
        }
        mStateSender.sendBroadcast(IAction.ACTION_READ_THREAD_SHUTDOWN, e);
    }
}
