package com.littlebayreal.easysocketlib.protocol;

import com.littlebayreal.easysocketlib.interfaces.protocol.IByteEscape;
import com.littlebayreal.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.littlebayreal.easysocketlib.util.BitOperator;

import java.nio.ByteOrder;

/**
 * 通用的协议解析规则
 * create by LiTtleBayReal
 */
public class CommonReaderProtocol implements IReaderProtocol {
	/**
	 * 解析方式 分为两种 通过分隔符解析以及通过整包长度解析
	 */
	public static final int PROTOCOL_RESOLUTION_BY_DELIMITER = 0x0001;//通过首尾分隔符截取报文才会有转义操作
	public static final int PROTOCOL_RESOLUTION_BY_PACKAGE_LENGTH = 0x0002;//通过长度截取报文不会有转义操作

	private int mResolveType;
	/**
	 * 分隔符
	 */
	private int mDelimiter;
	/**
	 * 定义bodylength在第几个字节开始
	 */
	private int mBodyLengthIndex;
	/**
	 * 定义bodylength字节有多少长度
	 */
	private int mBodyLengthSize;
	/**
	 * 定义包头的长度
	 */
	private int mHeaderLength;
	/**
	 * 如果选择解析方式为分隔符解析，那么转义处理类不能为空
	 */
	private IByteEscape mIByteEscape;
	/**
	 * 是否开启校验位
	 */
	private boolean isOpenCheck;
	/**
	 * 是否开启分隔符
	 */
	private boolean isDelimiter;
	public CommonReaderProtocol(int mResolveType,int mHeaderLength,boolean isDelimiter,int mDelimiter,boolean isOpenCheck) {
		this(mResolveType,isDelimiter,mDelimiter, mHeaderLength,isOpenCheck,null);
	}

	public CommonReaderProtocol(int mResolveType,boolean isDelimiter,int mDelimiter,int mHeaderLength,boolean isOpenCheck,IByteEscape mIByteEscape) {
		this.mResolveType = mResolveType;
		this.isDelimiter = isDelimiter;
		this.mDelimiter = mDelimiter;
		this.mHeaderLength = mHeaderLength;
		this.isOpenCheck = isOpenCheck;
		this.mIByteEscape = mIByteEscape;
	}

	/**
	 * 包头肯定是固定死的
	 * @return
	 */
	@Override
	public int getHeaderLength() {
		return mHeaderLength;
	}

	@Override
	public int getHeaderLength(byte[] data) {
		return 0;
	}
	/**
	 * 获取协议中已确定的报文分隔符
	 * @return
	 */
	public int getDelimiter(){
		return mDelimiter;
	}

	public int getmResolveType() {
		return mResolveType;
	}

	public boolean isOpenCheck() {
		return isOpenCheck;
	}

	public boolean isDelimiter() {
		return isDelimiter;
	}

	public IByteEscape getmIByteEscape() {
		return mIByteEscape;
	}

	/**
	 * 注:如果采用的是首尾分隔符，此方法可以忽略
	 * @param header    根据getHeaderLength()方法获得的包头原始数据.开发者应该从此header种解析出包体长度数据.
	 * @param byteOrder 当前包头字节数组种,包头数据的字节序类型.
	 * @return
	 */
	@Override
	public int getBodyLength(byte[] header, ByteOrder byteOrder) {
		//通过头部解析的报文 解析报文的body长度
		int msgProp = BitOperator.byteToInteger(BitOperator.splitBytes(header, 3, 4));
		int msgLength = msgProp & 0x3ff;
		return msgLength + 1 + 1;
	}
}
