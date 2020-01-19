package com.sziti.easysocketlib.client;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.base.AbsLoopThread;
import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.io.IStateSender;
import com.sziti.easysocketlib.interfaces.send.IResend;
import com.sziti.easysocketlib.util.HexStringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 补发控制器
 * Create by LiTtleBayReal
 */
public class ResendManager implements IResend<Serializable> {
	/**
	 * 数据包发送器
	 */
	private volatile IConnectionManager mManager;
	/**
	 * 状态机
	 *
	 * @param IStateSender
	 */
	private IStateSender mSender;
	/**
	 * 补发队列
	 */
	private List<BaseSendData> mResendList = Collections.synchronizedList(new ArrayList<BaseSendData>());
	/**
	 * 补发线程
	 *
	 * @param data
	 */
	private ResendThread mResendThread = null;
	/**
	 * 是否死掉
	 */
	private volatile boolean isDead = false;


	public ResendManager(IConnectionManager iConnectionManager, IStateSender iStateSender) {
		mManager = iConnectionManager;
		mSender = iStateSender;
		//注册对socket发送情况的监听
	}

	@Override
	public void add(Serializable data) {
		BaseSendData d = (BaseSendData) data;
		synchronized (this) {
			if (!mResendList.contains(d))
				mResendList.add(d);
		}
	}

	@Override
	public void remove(Serializable data) {
		synchronized (this) {
			int size = mResendList.size();
			for (int k = 0; k < size; k++) {
//				SLog.i("ResendManager.remove()------" + getReissueQueue().get(k).generateBodyBytes()[0] + ":MsgID:" + msgid);
//				if (mResendList.get(k).generateBodyBytes()[0] == msgid) {
//					SLog.i( "ResendManager.remove()------通过应答将补发队列中的对应数据移除");
//					mResendList.remove(k);
//				}
			}
		}
	}

	@Override
	public void dead() {
		//作为一个补传的机制，防止过多的补传导致管道堵塞
		if (mResendList.size() > 999)
		mResendList.clear();
		isDead = true;
		privateDead();
	}

	/**
	 * 开始重发的任务线程
	 */
    public void startEngine(){
        if (mResendThread == null) {
			mResendThread = new ResendThread();
			//默认5秒执行一次重发线程 所以重发的间隔都是大于等于5秒并且以5的倍数增长
			mResendThread.setInterval(5000);
			mResendThread.start();
		}
	}
	private void privateDead() {
		if (mResendThread != null) {
			mResendThread.shutdown();
			mResendThread = null;
		}
	}

	class ResendThread extends AbsLoopThread {
		private BaseSendData data;

		@Override
		protected void runInLoopThread() throws Exception {
			if (isDead) {
				shutdown();
				return;
			}
			if (mManager != null && mResendList.size() > 0) {
				int size = mResendList.size();
				for (int i = size - 1; i >= 0; i--) {
					data = mResendList.get(i);
					//先发送 再移除

					SLog.i("ResendThread------补发数据:" + HexStringUtils.toHexString(data.parse()));
					//补发前处理 点位上传补发需要修改msgid
					//补发后处理
					if (!data.isReSend() || data.getSendTimes() <= 0) {
						SLog.i("ResendThread------移除补传");
						mResendList.remove(data);
					}
					//进行消息的补传操作
					for (BaseSendData s : mResendList) {
//						SLog.i( "ResendThread------补传消息第:" + s.getSendTimes() + "次发送" + s.getMsgHeader().getMsgId());
						if (!s.checkResend()) {
//							SLog.i( "ResendThread------补传消息时间验证失败" + s.getMsgHeader().getMsgId());
							continue;
						}
						//补传操作
						mManager.send(s);
						//设置本次补发的时间戳
						s.setSendStamp(System.currentTimeMillis());
						s.setSendTimes(s.getSendTimes() - 1);
//							SLog.i("ResendThread------发送补传消息:" + s.getMsgHeader().getMsgId()
//								+ ",FlowID:" + s.getMsgHeader().getFlowId());
//						}
					}
				}
			}
		}

		@Override
		protected void loopFinish(Exception e) {

		}
	}
}
