package com.littlebayreal.easysocketlib.config;

import java.nio.charset.Charset;

/**
 * Created by ZhangYanYan on 06/12/2019.
 * 所有通信协议配置
 * 平台端口、平台协议、所有设备配置和协议
 */
public interface APIConfig {
    /*--------------------------------------- 平台协议 ---------------------------------------*/
    int pkg_delimiter = 0x7E;
    int pkg_delimiter_end = 0x7F;
    //终端的唯一标识 用于让后台识别socket通道
    String terminal_id = "10512909090";
	Charset string_charset = Charset.forName("GBK");
	//终端上传通用标识
	int msg_id_device_common_upload = 0x0900;
}
