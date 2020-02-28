package com.sziti.easysocketlib.client.delegate.io;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.exceptions.ReadException;
import com.sziti.easysocketlib.interfaces.action.IOAction;
import com.sziti.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.sziti.easysocketlib.util.HexStringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by xuhao on 2017/5/31.
 * 解析类的具体实现
 */

public class ReaderImpl extends AbsReader {
    //残留的数据
    private ByteBuffer mRemainingBuf;

    @Override
    public void read() throws RuntimeException {
        OriginalData originalData = new OriginalData();
        //获取数据协议
        IReaderProtocol headerProtocol = mOkOptions.getReaderProtocol();
        //获取头部协议长度
        int headerLength = headerProtocol.getHeaderLength();
        //创建一个头部大小的byte数组
        ByteBuffer headBuf = ByteBuffer.allocate(headerLength);
        //设置byte数组是大端在前还是小端在前
        headBuf.order(mOkOptions.getReadByteOrder());
        try {
        	//处理剩余字节
            if (mRemainingBuf != null) {
                mRemainingBuf.flip();
                //比较缓存区剩余字节和头协议长度最小
                int length = Math.min(mRemainingBuf.remaining(), headerLength);
                headBuf.put(mRemainingBuf.array(), 0, length);
                //如果剩余字节小于头字节长度定义
                if (length < headerLength) {
                    //there are no data left
                    mRemainingBuf = null;
                    //从通道读取剩余的字节
                    readHeaderFromChannel(headBuf, headerLength - length);
                } else {
                    mRemainingBuf.position(headerLength);
                }
            } else {
                //读取报文头
                readHeaderFromChannel(headBuf, headBuf.capacity());
            }
            //设置回传数据的头
            originalData.setHeadBytes(headBuf.array());
            if (SLog.isDebug()) {
                SLog.i("read head: " + HexStringUtils.toHexString(headBuf.array()));
            }
            //从头部定义拿到报文的长度
            int bodyLength = headerProtocol.getBodyLength(originalData.getHeadBytes(), mOkOptions.getReadByteOrder());
            if (SLog.isDebug()) {
                SLog.i("need read body length: " + bodyLength);
            }
            if (bodyLength > 0) {
                if (bodyLength > mOkOptions.getMaxReadDataMB() * 1024 * 1024) {
                    throw new ReadException("Need to follow the transmission protocol.\r\n" +
                            "Please check the client/server code.\r\n" +
                            "According to the packet header data in the transport protocol, the package length is " + bodyLength + " Bytes.\r\n" +
                            "You need check your <ReaderProtocol> definition");
                }
                ByteBuffer byteBuffer = ByteBuffer.allocate(bodyLength);
                byteBuffer.order(mOkOptions.getReadByteOrder());
                if (mRemainingBuf != null) {
                    int bodyStartPosition = mRemainingBuf.position();
                    int length = Math.min(mRemainingBuf.remaining(), bodyLength);
                    byteBuffer.put(mRemainingBuf.array(), bodyStartPosition, length);
                    mRemainingBuf.position(bodyStartPosition + length);
                    //如果剩余的字节刚好等于报文长度
                    if (length == bodyLength) {
                        if (mRemainingBuf.remaining() > 0) {//there are data left 将剩下的数据保存
                            ByteBuffer temp = ByteBuffer.allocate(mRemainingBuf.remaining());
                            temp.order(mOkOptions.getReadByteOrder());
                            temp.put(mRemainingBuf.array(), mRemainingBuf.position(), mRemainingBuf.remaining());
                            mRemainingBuf = temp;
                        } else {//there are no data left  无数据清空
                            mRemainingBuf = null;
                        }
                        //拿到body内容  并且将解析好的报文对象回传
                        //cause this time data from remaining buffer not from channel.
                        originalData.setBodyBytes(byteBuffer.array());
                        mStateSender.sendBroadcast(IOAction.ACTION_READ_COMPLETE, originalData);
                        return;
                    } else {//there are no data left in buffer and some data pieces in channel
                        mRemainingBuf = null;
                    }
                }
                //读取body
                readBodyFromChannel(byteBuffer);
                originalData.setBodyBytes(byteBuffer.array());
            } else if (bodyLength == 0) {
                originalData.setBodyBytes(new byte[0]);
                if (mRemainingBuf != null) {
                    //the body is empty so header remaining buf need set null
                    if (mRemainingBuf.hasRemaining()) {
                        ByteBuffer temp = ByteBuffer.allocate(mRemainingBuf.remaining());
                        temp.order(mOkOptions.getReadByteOrder());
                        temp.put(mRemainingBuf.array(), mRemainingBuf.position(), mRemainingBuf.remaining());
                        mRemainingBuf = temp;
                    } else {
                        mRemainingBuf = null;
                    }
                }
            } else if (bodyLength < 0) {
                throw new ReadException(
                        "read body is wrong,this socket input stream is end of file read " + bodyLength + " ,that mean this socket is disconnected by server");
            }
            mStateSender.sendBroadcast(IOAction.ACTION_READ_COMPLETE, originalData);
        } catch (Exception e) {
            ReadException readException = new ReadException(e);
            throw readException;
        }
    }

    private void readHeaderFromChannel(ByteBuffer headBuf, int readLength) throws IOException {
        for (int i = 0; i < readLength; i++) {
            byte[] bytes = new byte[1];
            int value = mInputStream.read(bytes);
            //如果从输入流中读取的长度等于-1，那么意味着连接管道已断开，抛出错误，框架会自动处理
            if (value == -1) {
                throw new ReadException(
                        "read head is wrong, this socket input stream is end of file read " + value + " ,that mean this socket is disconnected by server");
            }
            headBuf.put(bytes);
        }
    }

    private void readBodyFromChannel(ByteBuffer byteBuffer) throws IOException {
    	//缓存区还有缓存空间就一直读取
        while (byteBuffer.hasRemaining()) {
            try {
                byte[] bufArray = new byte[mOkOptions.getReadPackageBytes()];
                int len = mInputStream.read(bufArray);
                if (len == -1) {
                    break;
                }
                int remaining = byteBuffer.remaining();
                //如果底层缓存区数据长度大于byteBuffer长度 那么就保存到mRemainingBuf
                if (len > remaining) {
                	//bytebuff只是收集到容量最大的字节数量 接下去的会放到mRemainingBuf等待下一次处理
                    byteBuffer.put(bufArray, 0, remaining);
                    mRemainingBuf = ByteBuffer.allocate(len - remaining);
                    mRemainingBuf.order(mOkOptions.getReadByteOrder());
                    mRemainingBuf.put(bufArray, remaining, len - remaining);
                } else {
                    byteBuffer.put(bufArray, 0, len);
                }
            } catch (Exception e) {
                throw e;
            }
        }
        if (SLog.isDebug()) {
            SLog.i("read total bytes: " + HexStringUtils.toHexString(byteBuffer.array()));
            SLog.i("read total length:" + (byteBuffer.capacity() - byteBuffer.remaining()));
        }
    }

}
