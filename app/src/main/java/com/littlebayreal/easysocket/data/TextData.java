package com.littlebayreal.easysocket.data;

import com.littlebayreal.easysocketlib.client.pojo.BaseSendData;
import com.littlebayreal.easysocketlib.config.APIConfig;
import com.littlebayreal.easysocketlib.util.BitOperator;

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
