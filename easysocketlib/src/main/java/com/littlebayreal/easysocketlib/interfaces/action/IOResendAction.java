package com.littlebayreal.easysocketlib.interfaces.action;

public interface IOResendAction extends IOAction{
	//发送报文出现异常 需要放入重发队列  重新发送
	String ACTION_RESEND_REQUEST = "action_resend_request";
}
