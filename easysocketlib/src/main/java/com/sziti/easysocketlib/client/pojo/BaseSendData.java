package com.sziti.easysocketlib.client.pojo;

import com.sziti.easysocketlib.config.APIConfig;
import com.sziti.easysocketlib.interfaces.protocol.IHeaderProtocol;
import com.sziti.easysocketlib.interfaces.send.ISendable;
import com.sziti.easysocketlib.util.BitOperator;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseSendData implements ISendable {
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

	@Override
	public void setSerialNum(int serialNum) {
		mProtocolHeader.setSerialNum(serialNum);
	}

	@Override
	public byte[] parse() {
		byte[] bodybytes = generateBodyBytes();
		mProtocolHeader.setBodyLength(bodybytes.length);
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
}
