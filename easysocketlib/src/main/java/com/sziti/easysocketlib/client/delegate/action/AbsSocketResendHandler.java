package com.sziti.easysocketlib.client.delegate.action;

import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.interfaces.action.IOResendAction;
import com.sziti.easysocketlib.interfaces.action.ISocketActionListener;
import com.sziti.easysocketlib.interfaces.action.ISocketResendActionListener;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.dispatcher.IRegister;
import com.sziti.easysocketlib.interfaces.send.IResend;
import com.sziti.easysocketlib.interfaces.send.ISendable;

public abstract class AbsSocketResendHandler extends SocketActionAdapter implements ISocketResendActionListener {
	protected IConnectionManager mManager;
	protected IResend iResend;

	/**
	 * 注册socket监听
	 *
	 * @param manager
	 * @param register
	 */
	public void attach(IConnectionManager manager, IResend<BaseSendData> iResend, IRegister<ISocketActionListener, IConnectionManager> register) {
		this.mManager = manager;
		this.iResend = iResend;
		//实际调用的是AbsConnectionManager中的registerReceiver方法 actionDispatcher获得actionhandler
		register.registerReceiver(this);
	}

	public void detach(IRegister register) {
		register.unRegisterReceiver(this);
	}

	public void addForResend(ISendable iSendable) {
		if (iResend != null)
			iResend.add(iSendable);
	}
//    public abstract void removeForResend();

	public void dead() {
		if (iResend != null)
			iResend.dead();
	}
}
