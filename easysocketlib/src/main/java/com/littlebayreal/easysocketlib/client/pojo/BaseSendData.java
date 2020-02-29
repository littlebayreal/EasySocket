package com.littlebayreal.easysocketlib.client.pojo;

import com.littlebayreal.easysocketlib.client.delegate.io.AbsSendable;
import com.littlebayreal.easysocketlib.config.APIConfig;
import com.littlebayreal.easysocketlib.interfaces.protocol.IHeaderProtocol;
import com.littlebayreal.easysocketlib.util.BitOperator;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSendData extends AbsSendable {
	//协议头
    private IHeaderProtocol mProtocolHeader;

	//是否补发 true:补发  false:关闭补发 默认情况下为不启动补发
	protected boolean reSend = false;
	//发送的时间戳
	protected long sendStamp = 0L;
	//补发的次数 默认三次
	protected int sendTimes = 3;

	//向后台发送数据时需要自己手动拼装数据
	public abstract byte[] generateBodyBytes();

	public void setHeaderProtocol(IHeaderProtocol iHeaderProtocol){
		this.mProtocolHeader = iHeaderProtocol;
	}
	@Override
	public byte[] parse() {
		if (mProtocolHeader == null)
			throw new RuntimeException("IHeaderProtocol is Null");
		byte[] bodybytes = generateBodyBytes();
		mProtocolHeader.setBodyLength(bodybytes.length);
		mProtocolHeader.setSerialNum(mSerialNum);
		byte[] headbytes = mProtocolHeader.getHeaderBytes();
		List<byte[]> listbytes = new ArrayList<>();
		//添加消息头
		listbytes.add(headbytes);
		//添加消息体
		listbytes.add(bodybytes);
		byte[] srcbytes = BitOperator.concatAll(listbytes);
		byte[] check = new byte[]{(byte) (BitOperator.getCheckSum4JT808(srcbytes, 0, srcbytes.length))};
		byte[] flag = new byte[]{APIConfig.pkg_delimiter};
		byte[] sendbytes = BitOperator.concatAll(flag, srcbytes, check, flag);
		return sendbytes;
	}

	public boolean isReSend() {
		return reSend;
	}

	public void setReSend(boolean reSend) {
		this.reSend = reSend;
	}

	public long getSendStamp() {
		return sendStamp;
	}

	public void setSendStamp(long sendStamp) {
		this.sendStamp = sendStamp;
	}

	public int getSendTimes() {
		return sendTimes;
	}

	public void setSendTimes(int sendTimes) {
		this.sendTimes = sendTimes;
	}
	public boolean checkResend() {
		return System.currentTimeMillis() - sendStamp >=(4 - sendTimes) * 10000;
	}
}
