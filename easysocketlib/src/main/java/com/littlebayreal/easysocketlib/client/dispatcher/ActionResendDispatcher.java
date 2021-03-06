package com.littlebayreal.easysocketlib.client.dispatcher;

import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.client.delegate.action.AbsSocketResendHandler;
import com.littlebayreal.easysocketlib.client.pojo.BaseSendData;
import com.littlebayreal.easysocketlib.client.pojo.OriginalData;
import com.littlebayreal.easysocketlib.interfaces.action.ISocketActionListener;
import com.littlebayreal.easysocketlib.interfaces.connection.IConnectionManager;
import com.littlebayreal.easysocketlib.interfaces.send.IPulseSendable;
import com.littlebayreal.easysocketlib.interfaces.send.ISendable;

import java.io.Serializable;

import static com.littlebayreal.easysocketlib.interfaces.action.IAction.ACTION_CONNECTION_FAILED;
import static com.littlebayreal.easysocketlib.interfaces.action.IAction.ACTION_CONNECTION_SUCCESS;
import static com.littlebayreal.easysocketlib.interfaces.action.IAction.ACTION_DISCONNECTION;
import static com.littlebayreal.easysocketlib.interfaces.action.IAction.ACTION_READ_THREAD_SHUTDOWN;
import static com.littlebayreal.easysocketlib.interfaces.action.IAction.ACTION_READ_THREAD_START;
import static com.littlebayreal.easysocketlib.interfaces.action.IAction.ACTION_WRITE_THREAD_SHUTDOWN;
import static com.littlebayreal.easysocketlib.interfaces.action.IAction.ACTION_WRITE_THREAD_START;
import static com.littlebayreal.easysocketlib.interfaces.action.IOAction.ACTION_PULSE_REQUEST;
import static com.littlebayreal.easysocketlib.interfaces.action.IOAction.ACTION_READ_COMPLETE;
import static com.littlebayreal.easysocketlib.interfaces.action.IOAction.ACTION_WRITE_COMPLETE;
import static com.littlebayreal.easysocketlib.interfaces.action.IOResendAction.ACTION_RESEND_REQUEST;

public class ActionResendDispatcher extends ActionDispatcher{
	public ActionResendDispatcher(ConnectionInfo info, IConnectionManager manager) {
		super(info, manager);
	}
	@Override
	public void dispatchActionToListener(String action, Serializable arg, ISocketActionListener responseHandler) {
		switch (action) {
			case ACTION_CONNECTION_SUCCESS: {
				try {
					responseHandler.onSocketConnectionSuccess(mConnectionInfo, action);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case ACTION_CONNECTION_FAILED: {
				try {
					Exception exception = (Exception) arg;
					responseHandler.onSocketConnectionFailed(mConnectionInfo, action, exception);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case ACTION_DISCONNECTION: {
				try {
					Exception exception = (Exception) arg;
					responseHandler.onSocketDisconnection(mConnectionInfo, action, exception);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case ACTION_READ_COMPLETE: {
				try {
					OriginalData data = (OriginalData) arg;
					responseHandler.onSocketReadResponse(mConnectionInfo, action, data);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case ACTION_READ_THREAD_START:
			case ACTION_WRITE_THREAD_START: {
				try {
					responseHandler.onSocketIOThreadStart(action);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case ACTION_WRITE_COMPLETE: {
				try {
					ISendable sendable = (ISendable) arg;
					responseHandler.onSocketWriteResponse(mConnectionInfo, action, sendable);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case ACTION_RESEND_REQUEST://需要重发的报文 （2020/8/21 0.1.0 修复重发导致的类型转换错误）
				try {
					BaseSendData sendable = (BaseSendData) arg;
					AbsSocketResendHandler resendActionAdapter = (AbsSocketResendHandler) responseHandler;
					resendActionAdapter.onSocketWriteFailed(action, sendable);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case ACTION_WRITE_THREAD_SHUTDOWN:
			case ACTION_READ_THREAD_SHUTDOWN: {
				try {
					Exception exception = (Exception) arg;
					responseHandler.onSocketIOThreadShutdown(action, exception);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case ACTION_PULSE_REQUEST: {
				try {
					IPulseSendable sendable = (IPulseSendable) arg;
					responseHandler.onPulseSend(mConnectionInfo, sendable);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
}
