package com.littlebayreal.easysocketlib.interfaces.connection;


import com.littlebayreal.easysocketlib.base.EasySocketOptions;

/**
 * Created by xuhao on 2017/5/16.
 */

public interface IConfiguration {
    /**
     * 修改参数配置
     *
     * @param okOptions 新的参数配置
     * @return 当前的链接管理器
     */
    IConnectionManager option(EasySocketOptions okOptions);

    /**
     * 获得当前连接管理器的参数配置
     *
     * @return 参数配置
     */
    EasySocketOptions getOption();
}
