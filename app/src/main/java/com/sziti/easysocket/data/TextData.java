package com.sziti.easysocket.data;

import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.config.APIConfig;
import com.sziti.easysocketlib.util.BitOperator;

import java.util.ArrayList;
import java.util.List;

public class TextData extends BaseSendData {
	private String message;
	public TextData(String message){
		this.message = message;
	}
	@Override
	public byte[] generateBodyBytes() {
		List<byte[]> listbyte = new ArrayList<>();
		//终端制造商ID
		byte[] temp = message.getBytes(APIConfig.string_charset);
		listbyte.add(temp);
		return BitOperator.concatAll(listbyte);
	}
}
