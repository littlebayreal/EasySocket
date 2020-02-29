package com.littlebayreal.easysocketlib.interfaces.io;

import com.littlebayreal.easysocketlib.interfaces.protocol.IReaderProtocol;

import java.nio.ByteOrder;

public interface IIOCoreOptions {
    //设置byte数组的组装格式 大端在前还是小端在前
    ByteOrder getReadByteOrder();

    int getMaxReadDataMB();

    IReaderProtocol getReaderProtocol();

    ByteOrder getWriteByteOrder();

    int getReadPackageBytes();

    int getWritePackageBytes();

    boolean isDebug();
    //打开流水号
//    boolean getIsOpenSerialNum();
}
