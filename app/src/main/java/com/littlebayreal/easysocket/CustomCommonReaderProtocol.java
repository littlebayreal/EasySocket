package com.littlebayreal.easysocket;

import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.interfaces.protocol.IByteEscape;
import com.littlebayreal.easysocketlib.protocol.CommonReaderProtocol;
import com.littlebayreal.easysocketlib.util.BitOperator;
import com.littlebayreal.easysocketlib.util.HexStringUtils;

public class CustomCommonReaderProtocol extends CommonReaderProtocol {
	private static final String TAG = "CustomCommonReaderProto";
	public CustomCommonReaderProtocol(int mResolveType, int mHeaderLength, boolean isDelimiter, int mDelimiter, boolean isOpenCheck) {
		super(mResolveType, mHeaderLength, isDelimiter, mDelimiter, isOpenCheck);
	}

	public CustomCommonReaderProtocol(int mResolveType, boolean isDelimiter, int mDelimiter, int mHeaderLength, boolean isOpenCheck, IByteEscape mIByteEscape) {
		super(mResolveType, isDelimiter, mDelimiter, mHeaderLength, isOpenCheck, mIByteEscape);
	}

	@Override
	public int getHeaderLength(byte[] data) {
		//特殊处理 7e 0 0 0 0 7e
		if (BitOperator.byteToInteger(BitOperator.splitBytes(data, 1, 2)) == 0) {
			SLog.i("特殊报文:"+ HexStringUtils.toHexString(data));
			return 4;
		} else
			return super.getHeaderLength();
	}
}
