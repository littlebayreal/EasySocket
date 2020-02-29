package com.sziti.easysocketlib.client;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.base.AbsLoopThread;
import com.sziti.easysocketlib.base.EasySocketOptions;
import com.sziti.easysocketlib.exceptions.DogDeadException;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.send.IPulse;
import com.sziti.easysocketlib.interfaces.send.IPulseSendable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xuhao on 2017/5/18.
 */

public class PulseManager implements IPulse {
    /**
     * 数据包发送器
     */
    private volatile IConnectionManager mManager;
    /**
     * 心跳数据包
     */
    private IPulseSendable mSendable;
    /**
     * 连接参数
     */
    private volatile EasySocketOptions mOkOptions;
    /**
     * 当前频率
     */
    private volatile long mCurrentFrequency;
    /**
     * 当前的线程模式
     */
    private volatile EasySocketOptions.IOThreadMode mCurrentThreadMode;
    /**
     * 是否死掉
     */
    private volatile boolean isDead = false;
    /**
     * 允许遗漏的次数
     */
    private volatile AtomicInteger mLoseTimes = new AtomicInteger(-1);

    private PulseThread mPulseThread = new PulseThread();

    public PulseManager(IConnectionManager manager, EasySocketOptions okOptions) {
        mManager = manager;
        mOkOptions = okOptions;
        mCurrentThreadMode = mOkOptions.getIOThreadMode();
    }

    public synchronized IPulse setPulseSendable(IPulseSendable sendable) {
        if (sendable != null) {
            mSendable = sendable;
        }
        return this;
    }

    public IPulseSendable getPulseSendable() {
        return mSendable;
    }

    //心跳的初始化
    @Override
    public synchronized void pulse() {
        privateDead();
        updateFrequency();
        //心跳只有在双工的情况下才会有
        if (mCurrentThreadMode != EasySocketOptions.IOThreadMode.SIMPLEX) {
            if (mPulseThread.isShutdown()) {
                mPulseThread.start();
            }
        }
    }
    //触发首次心跳
    @Override
    public synchronized void trigger() {
        if (isDead) {
            return;
        }
        //单通道模式下不进行心跳发送
        if (mCurrentThreadMode != EasySocketOptions.IOThreadMode.SIMPLEX && mManager != null && mSendable != null) {
            mManager.send(mSendable);
        }
    }
    //杀死心跳
    public synchronized void dead() {
        mLoseTimes.set(0);
        isDead = true;
        privateDead();
    }
   //设置心跳的频率
    private synchronized void updateFrequency() {
        if (mCurrentThreadMode != EasySocketOptions.IOThreadMode.SIMPLEX) {
            mCurrentFrequency = mOkOptions.getPulseFrequency();
//            mCurrentFrequency = mCurrentFrequency < 1000 ? 1000 : mCurrentFrequency;//间隔最小为一秒
			if (mPulseThread != null)
				mPulseThread.setInterval(mCurrentFrequency);
        } else {
            privateDead();
        }
    }

    @Override
    public synchronized void feed() {
        mLoseTimes.set(-1);
    }

    private void privateDead() {
        if (mPulseThread != null) {
            mPulseThread.shutdown();
        }
    }

    public int getLoseTimes() {
        return mLoseTimes.get();
    }

    public synchronized void setOkOptions(EasySocketOptions okOptions) {
        mOkOptions = okOptions;
        mCurrentThreadMode = mOkOptions.getIOThreadMode();
        updateFrequency();
    }

    /**
     * 心跳线程
     */
    private class PulseThread extends AbsLoopThread {
        @Override
        protected void runInLoopThread() throws Exception {
            if (isDead) {
                shutdown();
                return;
            }
            if (mManager != null && mSendable != null) {
                if (mOkOptions.getPulseFeedLoseTimes() != -1 && mLoseTimes.incrementAndGet() >= mOkOptions.getPulseFeedLoseTimes()) {
                    //心跳过长没收到  断开socket连接
                    mManager.disconnect(new DogDeadException("you need feed dog on time,otherwise he will die"));
                } else {
                    mManager.send(mSendable);
                }
            }

            //not safety sleep.
//            Thread.sleep(mCurrentFrequency);
        }

        @Override
        protected void loopFinish(Exception e) {
        	SLog.e("PulseManager:"+ e.getMessage());
        }
    }
}
