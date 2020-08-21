package com.littlebayreal.easysocketlib.interfaces.send;

import com.littlebayreal.easysocketlib.interfaces.protocol.IWriterProtocol;

import java.io.Serializable;

/**
 * 可发送类,继承该类,并实现parse方法即可获得发送能力
 * Created by xuhao on 2017/5/16.
 */
public interface ISendable extends Serializable {
    /**
     * 数据转化
     *
     * @return 将要发送的数据的字节数组
     */
    byte[] parse();
	/**
	 * 将数据按照用户自定义的发送规则转化
	 * @param iWriterProtocol 定义的发送规则
	 * @return
	 */
	byte[] parse(IWriterProtocol iWriterProtocol) throws Exception;
}
