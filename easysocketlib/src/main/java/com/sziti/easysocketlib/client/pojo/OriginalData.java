package com.sziti.easysocketlib.client.pojo;

import com.sziti.easysocketlib.util.HexStringUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 原始数据结构体
 */
public class OriginalData implements Serializable {
    /**
     * 原始数据包头字节数组
     */
    private byte[] mHeadBytes;
    /**
     * 原始数据包体字节数组
     */
    private byte[] mBodyBytes;

    public byte[] getHeadBytes() {
        return mHeadBytes;
    }

    public void setHeadBytes(byte[] headBytes) {
        mHeadBytes = headBytes;
    }

    public byte[] getBodyBytes() {
        return mBodyBytes;
    }

    public void setBodyBytes(byte[] bodyBytes) {
        mBodyBytes = bodyBytes;
    }

	@Override
	public String toString() {
		return "OriginalData{" +
			"mHeadBytes=" + HexStringUtils.toHexString(mHeadBytes) +
			", mBodyBytes=" + HexStringUtils.toHexString(mBodyBytes) +
			'}';
	}
}
