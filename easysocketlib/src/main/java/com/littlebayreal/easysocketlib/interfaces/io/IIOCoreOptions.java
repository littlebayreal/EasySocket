package com.littlebayreal.easysocketlib.interfaces.io;

import com.littlebayreal.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.littlebayreal.easysocketlib.interfaces.protocol.IWriterProtocol;

import java.nio.ByteOrder;

public interface IIOCoreOptions {
    //设置byte数组的组装格式 大端在前还是小端在前
    ByteOrder getReadByteOrder();

    int getMaxReadDataMB();

    IReaderProtocol getReaderProtocol();
	//（2020/8/21 0.1.0 增加对写入数据的自定义转义策略）
    IWriterProtocol getWriterProtocol();

    ByteOrder getWriteByteOrder();

    int getReadPackageBytes();

    int getWritePackageBytes();

    boolean isDebug();
}
