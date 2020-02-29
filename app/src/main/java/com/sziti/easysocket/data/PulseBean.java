package com.sziti.easysocket.data;


import com.sziti.easysocketlib.client.pojo.BaseSendData;
import com.sziti.easysocketlib.config.APIConfig;
import com.sziti.easysocketlib.interfaces.send.IPulseSendable;
import com.sziti.easysocketlib.util.BitOperator;
import java.util.ArrayList;
import java.util.List;

public class PulseBean extends BaseSendData implements IPulseSendable {
	@Override
	public byte[] generateBodyBytes() {
		List<byte[]> listbyte = new ArrayList<>();
		//终端制造商ID
		byte[] temp = "我是心跳包".getBytes(APIConfig.string_charset);
		listbyte.add(temp);
		return BitOperator.concatAll(listbyte);
	}
}
