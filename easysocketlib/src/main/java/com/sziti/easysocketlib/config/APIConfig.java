package com.sziti.easysocketlib.config;

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
	//终端上传通用标识
	int msg_id_device_common_upload = 0x0900;
    // 标识位：上行（设备→网络中心）
    int pkg_delimiter_push = 0xFF;
    // 标识位：下行（网络中心→设备）
    int pkg_delimiter_receive = 0xFE;
    // 重启
    int msg_id_restart = 0x00;
    // 心跳
    int msg_id_heart_beat = 0x01;
    // 设置静默
    int msg_id_set_silent = 0x03;
    // 打开报警
    int msg_id_open_alarm = 0x05;
    // 关闭报警
    int msg_id_close_alarm = 0x06;
    // 发送短信
    int msg_id_send_message = 0x0B;
    // 公交业务指令
    int msg_id_bus_business = 0x20;
    // 子命令公交调度信息
    int msg_id_deliver_operate_line = 0x00;
    // 子命令公交查询设备线路版本信息
    int msg_id_deliver_find_device_line_version = 0x01;
    // 子命令服务器端向设备发送调度指令
    int msg_id_deliver_send_operation_cmd = 0x02;
    // 文件下载请求
    int msg_id_download_file_request = 0x21;
    // 文件数据下发
    int msg_id_download_file = 0x22;
    // 文件下发完成
    int msg_id_download_file_complete = 0x23;
    // 文件下发终止、取消
    int msg_id_download_file_cancel = 0x24;
    // 定时上报状态信息
    int msg_id_upload_state_info = 0x50;
    // 心跳帧
    int msg_id_heart_beat_push = 0x56;
    // 到离站数据上传
    int msg_id_stop_info_push = 0x51;
    // 驾驶员登签
    int msg_id_driver_login_push = 0x54;
    // 驾驶员就绪
    int msg_id_driver_ready_push = 0x55;
    // 设备报警指令
    int msg_id_alarm_push = 0x52;
}
