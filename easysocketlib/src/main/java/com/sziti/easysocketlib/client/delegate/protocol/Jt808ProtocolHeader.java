package com.sziti.easysocketlib.client.delegate.protocol;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.config.APIConfig;
import com.sziti.easysocketlib.interfaces.protocol.IHeaderProtocol;
import com.sziti.easysocketlib.util.BCD8421Operater;
import com.sziti.easysocketlib.util.BitOperator;

import java.util.ArrayList;
import java.util.List;

public class Jt808ProtocolHeader implements IHeaderProtocol {
	// 消息ID
	protected int msgId;
	// 消息体长度
	protected int msgBodyLength;
	// 数据加密方式
	protected int encryptionType;
	// 是否分包,true==>有消息包封装项
	protected boolean hasSubPackage;
	// 保留位[14-15]
	protected int reservedBit;
	/////// ========消息体属性
	// 终端手机号
	protected String terminalPhone;
	// 消息流水号
	protected int flowId = 0;
	//////// =====消息包封装项
	// 消息包封装项 byte[12-15]
	protected int packageInfoField;
	// 消息包总数(word(16))
	protected long totalSubPackage;
	// 包序号(word(16))这次发送的这个消息包是分包中的第几个消息包, 从 1 开始
	protected long subPackageSeq;
	//////// =====消息包封装项
	public static class Builder {
		private Jt808ProtocolHeader mJt808ProtocolHeader;

		public Builder() {
			this(Jt808ProtocolHeader.getDefault());
		}

		public Builder(Jt808ProtocolHeader okOptions) {
			mJt808ProtocolHeader = okOptions;
		}

		public Builder setMsgId(int msgId){
            mJt808ProtocolHeader.setMsgId(msgId);
			return this;
		}
		public Builder setMsgBodyLength(int msgBodyLength){
			mJt808ProtocolHeader.setMsgBodyLength(msgBodyLength);
			return this;
		}
		public Builder setEncryptionType(int encryptionType){
			mJt808ProtocolHeader.setEncryptionType(encryptionType);
			return this;
		}
		public Builder setHasSubPackage(boolean hasSubPackage){
			mJt808ProtocolHeader.setHasSubPackage(hasSubPackage);
			return this;
		}
		public Builder setReservedBit(int reservedBit){
			mJt808ProtocolHeader.setReservedBit(reservedBit);
			return this;
		}
		public Builder setTerminalPhone(String terminalPhone){
			mJt808ProtocolHeader.setTerminalPhone(terminalPhone);
			return this;
		}
		public Builder setTotalSubPackage(int totalSubPackage){
			mJt808ProtocolHeader.setTotalSubPackage(totalSubPackage);
			return this;
		}
		public Builder setSubPackageSeq(int subPackageSeq){
			mJt808ProtocolHeader.setSubPackageSeq(subPackageSeq);
			return this;
		}
	}

	/**
	 * Jt808协议头的默认配置
	 * @return
	 */
	private static Jt808ProtocolHeader getDefault() {
         Jt808ProtocolHeader mJt808ProtocolHeader = new Jt808ProtocolHeader();

         mJt808ProtocolHeader.setMsgId(APIConfig.msg_id_device_common_upload);
         //如果在生成发送体中，body为空，那么长度为0
         mJt808ProtocolHeader.setMsgBodyLength(0);
         //数据加密默认为0
         mJt808ProtocolHeader.setEncryptionType(0);
         mJt808ProtocolHeader.setHasSubPackage(false);
         mJt808ProtocolHeader.setReservedBit(0);
         mJt808ProtocolHeader.setTerminalPhone(APIConfig.terminal_id);
         mJt808ProtocolHeader.setFlowId(0);
         return mJt808ProtocolHeader;
	}

	public int getMsgId() {
		return msgId;
	}

	public void setMsgId(int msgId) {
		this.msgId = msgId;
	}

	public int getMsgBodyLength() {
		return msgBodyLength;
	}

	public void setMsgBodyLength(int msgBodyLength) {
		this.msgBodyLength = msgBodyLength;
	}

	public int getEncryptionType() {
		return encryptionType;
	}

	public void setEncryptionType(int encryptionType) {
		this.encryptionType = encryptionType;
	}

	public boolean isHasSubPackage() {
		return hasSubPackage;
	}

	public void setHasSubPackage(boolean hasSubPackage) {
		this.hasSubPackage = hasSubPackage;
	}

	public int getReservedBit() {
		return reservedBit;
	}

	public void setReservedBit(int reservedBit) {
		this.reservedBit = reservedBit;
	}

	public String getTerminalPhone() {
		return terminalPhone;
	}

	public void setTerminalPhone(String terminalPhone) {
		this.terminalPhone = terminalPhone;
	}

	public int getFlowId() {
		return flowId;
	}
	//在报文发送成功后都要将流水号更新
	public void setFlowId(int flowId) {
		this.flowId = flowId;
	}

	public int getPackageInfoField() {
		return packageInfoField;
	}

	public void setPackageInfoField(int packageInfoField) {
		this.packageInfoField = packageInfoField;
	}

	public long getTotalSubPackage() {
		return totalSubPackage;
	}

	public void setTotalSubPackage(long totalSubPackage) {
		this.totalSubPackage = totalSubPackage;
	}

	public long getSubPackageSeq() {
		return subPackageSeq;
	}

	public void setSubPackageSeq(long subPackageSeq) {
		this.subPackageSeq = subPackageSeq;
	}

	@Override
	public int setBodyLength(int bodyLength) {
		setMsgBodyLength(bodyLength);
		return bodyLength;
	}

	@Override
	public byte[] getHeaderBytes() {
		List<byte[]> listbytes = new ArrayList<>();
		// 消息ID
		listbytes.add(BitOperator.numToByteArray(this.getMsgId(), 2));

		// 消息体长度
		int msglength = this.getMsgBodyLength();
		SLog.i( "msglength:" + msglength);
		// 数据加密方式
		int encryptionType = this.getEncryptionType();
		// 分包
		int subpackage = 0;
		if (this.isHasSubPackage())
			subpackage = 1;
		// 保留位
		int reserve = this.getReservedBit();
		int msgmodel = (reserve << 14) + (subpackage << 13) + (encryptionType << 10) + msglength;
		SLog.i( "msgmodel:" + msgmodel);
		// 消息属性
		listbytes.add(BitOperator.numToByteArray(msgmodel, 2));


		// 终端手机号
		byte[] phone = BCD8421Operater.string2Bcd(this.getTerminalPhone());
		listbytes.add(phone);
		// 消息流水号
		byte[] flowid = BitOperator.numToByteArray(this.getFlowId(), 2);
		listbytes.add(flowid);
		// 消息包封装项
		if (this.isHasSubPackage()) {
			// 消息总包数
			listbytes.add(BitOperator.numToByteArray(this.getTotalSubPackage(), 2));
			// 包序号
			listbytes.add(BitOperator.numToByteArray(this.getTotalSubPackage(), 2));
		}

		byte[] msghead = BitOperator.concatAll(listbytes);
		return msghead;
	}
}
