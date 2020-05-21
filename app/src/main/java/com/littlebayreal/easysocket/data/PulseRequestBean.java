package com.littlebayreal.easysocket.data;


import com.littlebayreal.easysocketlib.client.pojo.BaseSendData;
import com.littlebayreal.easysocketlib.config.APIConfig;
import com.littlebayreal.easysocketlib.interfaces.send.IPulseSendable;
import com.littlebayreal.easysocketlib.util.BitOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * 心跳信息
 */
public class PulseRequestBean extends BaseSendData implements IPulseSendable {
    @Override
    public byte[] generateBodyBytes() {
        List<byte[]> listbyte = new ArrayList<>();
        //终端制造商ID
        byte[] temp = "我是心跳包".getBytes(APIConfig.string_charset);
        listbyte.add(temp);
        return BitOperator.concatAll(listbyte);
    }
}
