package com.sziti.easysocketlib.client.delegate.io;

import android.util.Log;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.exceptions.ReadException;
import com.sziti.easysocketlib.interfaces.protocol.IReaderProtocol;
import com.sziti.easysocketlib.protocol.CommonReaderProtocol;
import com.sziti.easysocketlib.util.BitOperator;
import com.sziti.easysocketlib.util.HexStringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ResendReaderImpl extends AbsReader {
	//残留的数据
	private ByteBuffer mRemainingBuf;

	//	private byte[] buffer = new byte[mOkOptions.getMaxReadDataMB()];
	@Override
	public void read() throws RuntimeException {
		//获取数据协议
		CommonReaderProtocol commonReaderProtocol = (CommonReaderProtocol) mOkOptions.getReaderProtocol();
		try {
			if (commonReaderProtocol.getmResolveType() == CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_PACKAGE_LENGTH) {
				OriginalData originalData = new OriginalData();
				//获取数据协议
				int headerLength = commonReaderProtocol.getHeaderLength();
				ByteBuffer headBuf = ByteBuffer.allocate(headerLength);
				//设置byte数组是大端在前还是小端在前
				headBuf.order(mOkOptions.getReadByteOrder());
				//处理残留的字节数组
				if (mRemainingBuf != null) {
					int delimiter = commonReaderProtocol.getDelimiter();
					mRemainingBuf.flip();

					int start = findDelimiterIndex(mRemainingBuf.array(), delimiter) - mRemainingBuf.position();
					if (start <= -1) {
						//没找到正确的分隔符，记录全部清除
						mRemainingBuf = null;
						return;
					}
					//比较缓存区剩余字节和头协议长度最小
					int length = Math.min(mRemainingBuf.remaining() - start, headerLength);
//					if (start != -1)
					headBuf.put(mRemainingBuf.array(), start + mRemainingBuf.position(), mRemainingBuf.remaining() - start);

					//如果剩余字节小于头字节长度定义
					if (length < headerLength) {
						//there are no data left
						mRemainingBuf = null;
						//从通道读取剩余的字节
						readHeaderFromChannel(headBuf, headerLength - length, 0);
					} else {
						mRemainingBuf.position(headerLength);
					}
				} else {
					readHeaderFromChannel(headBuf, headBuf.capacity(), commonReaderProtocol.getDelimiter());
				}
				//头部解析成功后 才可以解析body
				if (headBuf.remaining() > 0) {
					originalData.setHeadBytes(headBuf.array());
					int bodyLenIndex = commonReaderProtocol.getBodyLengthIndex();
					int bodyLenSize = commonReaderProtocol.getBodylengthSize();
					ByteBuffer bodyLen = headBuf.get(headBuf.array(), bodyLenIndex, bodyLenSize);
					int bodyLength = BitOperator.byteToInteger(bodyLen.array());
					int packageLength = commonReaderProtocol.getHeaderLength() + bodyLength + (commonReaderProtocol.isOpenCheck() ? 1 : 0) + 1;
					if (SLog.isDebug()) {
						SLog.i("read head: " + HexStringUtils.toHexString(headBuf.array()));
					}
					bodyLength = packageLength - commonReaderProtocol.getHeaderLength();
					if (SLog.isDebug()) {
						SLog.i("need read body length: " + bodyLength);
					}
					if (bodyLength > 0) {
						//计算出来的报文不可超过单条协议最大限制
						if (packageLength > mOkOptions.getMaxReadDataMB()) {
							throw new ReadException("Need to follow the transmission protocol.\r\n" +
								"Please check the client/server code.\r\n" +
								"According to the packet header data in the transport protocol, the package length is " + bodyLength + " Bytes.\r\n" +
								"You need check your <ReaderProtocol> definition");
						}
						ByteBuffer byteBuffer = ByteBuffer.allocate(bodyLength);
						byteBuffer.order(mOkOptions.getReadByteOrder());

						//读取body
						readBodyFromChannel(byteBuffer);
						originalData.setBodyBytes(byteBuffer.array());
					}
				} else {
					mRemainingBuf = null;
				}
			} else if (commonReaderProtocol.getmResolveType()  == CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER) {

			}
		} catch (Exception e) {
			ReadException readException = new ReadException(e);
			throw readException;
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

	/**
	 * 从通道读取字节流并返回读取了多少字节
	 */
	private void readHeaderFromChannel(ByteBuffer headBuf, int readLength, int delimiter) throws IOException {
		int size = 0;
		byte[] bytes = new byte[readLength];
		while (size < readLength) {
			int len = mInputStream.available();
			int value = mInputStream.read(bytes, size, len);
			if (value == -1) {
				throw new ReadException(
					"read head is wrong, this socket input stream is end of file read " + value + " ,that mean this socket is disconnected by server");
			}
			size += len;
		}
		if (delimiter == 0) {
			headBuf.put(bytes);
		} else {
			int start = findDelimiterIndex(bytes, delimiter);
			if (start != -1)
				headBuf.put(bytes, start, bytes.length);
			else
				headBuf.flip();
		}
	}

	private int findDelimiterIndex(byte[] bytes, int delimiter) {
		int start = -1;
		for (int i = 0; i < bytes.length; i++) {
			if (delimiter == bytes[i]) {
				start = i;
				break;
			}
		}
		return start;
	}
}
