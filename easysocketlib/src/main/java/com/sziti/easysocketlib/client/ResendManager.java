package com.sziti.easysocketlib.client;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.base.AbsLoopThread;
import com.sziti.easysocketlib.client.delegate.action.SocketActionAdapter;
import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.exceptions.DogDeadException;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.io.IStateSender;
import com.sziti.easysocketlib.interfaces.send.IResend;
import com.sziti.easysocketlib.util.HexStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 补发控制器
 * Create by LiTtleBayReal
 */
public class ResendManager implements IResend<BaseSendData> {
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
	public void add(BaseSendData data) {

	}

	@Override
	public void remove(BaseSendData data) {

	}

	@Override
	public void dead() {
		mResendList.clear();
		isDead = true;
		privateDead();
	}

	private void privateDead() {
		if (mResendThread != null) {
			mResendThread.shutdown();
		}
	}

	class ResendThread extends AbsLoopThread {
		private BaseSendData soc;

		@Override
		protected void runInLoopThread() throws Exception {
			if (isDead) {
				shutdown();
				return;
			}
			if (mManager != null && mResendList.size() > 0) {
				int size = mResendList.size();
				for (int i = size - 1; i >= 0; i--) {
					soc = mResendList.get(i);
					//先发送 再移除

					SLog.i("SocketClient,RissueThread------补发数据:" + HexStringUtils.toHexString(soc.parse()));
					//补发前处理 点位上传补发需要修改msgid
					//补发后处理
					if (!soc.isReSend() || soc.getSendTimes() <= 0) {
						SLog.i("SocketClient,RissueThread------移除补传");
						mResendList.remove(soc);
					}
					//进行消息的补传操作
					for (BaseSendData s : mResendList) {
//						SLog.i( "ResendThread------补传消息第:" + s.getSendTimes() + "次发送" + s.getMsgHeader().getMsgId());
						if (!s.checkResend()) {
//							SLog.i( "ResendThread------补传消息时间验证失败" + s.getMsgHeader().getMsgId());
							continue;
						}
//						if () {
						//补传操作
						mManager.send(s);
						//设置本次补发的时间戳
//							s.setSendStamp(System.currentTimeMillis());
//							s.setSendTimes(s.getSendTimes() - 1);
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
