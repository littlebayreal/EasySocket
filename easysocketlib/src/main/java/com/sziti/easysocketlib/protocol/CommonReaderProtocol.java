package com.sziti.easysocketlib.protocol;

import com.sziti.easysocketlib.interfaces.protocol.IByteEscape;
import com.sziti.easysocketlib.interfaces.protocol.IReaderProtocol;

import java.nio.ByteOrder;

/**
 * 通用的协议解析规则
 * create by LiTtleBayReal
 */
public class CommonReaderProtocol implements IReaderProtocol {
	/**
	 * 解析方式 分为两种 通过分隔符解析以及通过整包长度解析
	 */
	public static final int PROTOCOL_RESOLUTION_BY_DELIMITER = 0x0001;
	public static final int PROTOCOL_RESOLUTION_BY_PACKAGE_LENGTH = 0x0002;

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
	private boolean isOpenCheck;
	public CommonReaderProtocol(int mResolveType,int mHeaderLength,int mDelimiter, int mBodyLengthIndex, int mBodyLengthSize,boolean isOpenCheck) {
		this(mResolveType,mDelimiter,mBodyLengthIndex, mBodyLengthSize, mHeaderLength,isOpenCheck,null);
	}

	public CommonReaderProtocol(int mResolveType,int mDelimiter, int mBodyLengthIndex, int mBodyLengthSize, int mHeaderLength,boolean isOpenCheck,IByteEscape mIByteEscape) {
		this.mResolveType = mResolveType;
		this.mDelimiter = mDelimiter;
		this.mBodyLengthIndex = mBodyLengthIndex;
		this.mBodyLengthSize = mBodyLengthSize;
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
	/**
	 * 获取协议中已确定的报文分隔符
	 * @return
	 */
	public int getDelimiter(){
		return mDelimiter;
	}

	public int getmResovleType() {
		return mResolveType;
	}

	public boolean isOpenCheck() {
		return isOpenCheck;
	}

	public int getBodyLengthIndex(){
		return mBodyLengthSize;
	}

	public int getBodylengthSize(){
		return mBodyLengthSize;
	}
	@Override
	public int getBodyLength(byte[] header, ByteOrder byteOrder) {
		//do nothing
		return 0;
	}

}
