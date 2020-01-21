package com.sziti.easysocketlib.client.delegate.io;

import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.exceptions.ReadException;
import com.sziti.easysocketlib.interfaces.action.IOAction;
import com.sziti.easysocketlib.protocol.CommonReaderProtocol;
import com.sziti.easysocketlib.util.BitOperator;
import com.sziti.easysocketlib.util.HexStringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ResendReaderImpl extends AbsReader {
	//残留的数据
	private ByteBuffer mRemainingBuf;
	//针对首尾分隔符处理进行的数据缓存队列
	private ArrayList<Byte> byteList = new ArrayList();
	private byte[] buffer = new byte[mOkOptions.getMaxReadDataMB()];
	private int mRemainingLength;

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

					int length = 0;
					//有开启分隔符 要先找分隔符索引
					if (commonReaderProtocol.isDelimiter()) {
						int start = findDelimiterIndex(mRemainingBuf.array(), delimiter);
						if (start <= -1) {
							//没找到正确的分隔符，记录全部清除
							mRemainingBuf = null;
							return;
						}
						//比较缓存区剩余字节和头协议长度最小
						length = Math.min(mRemainingBuf.remaining() - start, headerLength);
						headBuf.put(mRemainingBuf.array(), start, mRemainingBuf.remaining() - start);
					} else {
						length = Math.min(mRemainingBuf.remaining(), headerLength);
						headBuf.put(mRemainingBuf.array(), 0, length);
					}
					//如果剩余字节小于头字节长度定义
					if (length < headerLength) {
						//there are no data left
						mRemainingBuf = null;
						//从通道读取剩余的字节
						readHeaderFromChannel(headBuf, headerLength - length, false, 0);
					} else {
						mRemainingBuf.position(headerLength);
					}
				} else {
					readHeaderFromChannel(headBuf, headBuf.capacity(), commonReaderProtocol.isDelimiter(), commonReaderProtocol.getDelimiter());
				}
				//头部解析成功后 才可以解析body
				headBuf.flip();
				if (headBuf.remaining() == commonReaderProtocol.getHeaderLength()) {
					originalData.setHeadBytes(headBuf.array());
					int bodyLenIndex = commonReaderProtocol.getBodyLengthIndex();
					int bodyLenSize = commonReaderProtocol.getBodylengthSize();
					ByteBuffer bodyLen = headBuf.get(headBuf.array(), bodyLenIndex, bodyLenSize);
					int bodyLength = BitOperator.byteToInteger(bodyLen.array());
					//整个包就分为头和身体两部分
					int packageLength = commonReaderProtocol.getHeaderLength() + bodyLength;
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

						if (mRemainingBuf != null) {
							int bodyStartPosition = mRemainingBuf.position();
							int length = Math.min(mRemainingBuf.remaining(), bodyLength);
							byteBuffer.put(mRemainingBuf.array(), bodyStartPosition, length);
							mRemainingBuf.position(bodyStartPosition + length);
							if (length == bodyLength) {
								if (mRemainingBuf.remaining() > 0) {//there are data left
									ByteBuffer temp = ByteBuffer.allocate(mRemainingBuf.remaining());
									temp.order(mOkOptions.getReadByteOrder());
									temp.put(mRemainingBuf.array(), mRemainingBuf.position(), mRemainingBuf.remaining());
									mRemainingBuf = temp;
								} else {//there are no data left
									mRemainingBuf = null;
								}
								//读取body
								readBodyFromChannel(byteBuffer);
								originalData.setBodyBytes(byteBuffer.array());
								//拿到body内容  并且将解析好的报文对象回传
								//cause this time data from remaining buffer not from channel.
								originalData.setBodyBytes(byteBuffer.array());
								mStateSender.sendBroadcast(IOAction.ACTION_READ_COMPLETE, originalData);
								return;
							} else {//there are no data left in buffer and some data pieces in channel
								mRemainingBuf = null;
							}
						}
					}
				} else {
					mRemainingBuf = null;
				}
			} else if (commonReaderProtocol.getmResolveType() == CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER) {
				if (!commonReaderProtocol.isDelimiter() || commonReaderProtocol.getDelimiter() == -1)
					throw new RuntimeException("use read by delimiter，please set delimiter and open setting first");
				readPackageFromChannel();
				//缓存区长度至少要大于最短包长 才会开始解析
				while (byteList.size() > (commonReaderProtocol.getHeaderLength() + (commonReaderProtocol.isOpenCheck() ? 1 : 0) + 1)) {
					int count = 0;
					int start = 0;
					//默认是接收信息的长度
					int end = 0;
					//需要处理的所有字节
					byte[] byte_temp = new byte[mRemainingLength];
					for (int j = 0; j < mRemainingLength; j++) {
						byte_temp[j] = byteList.get(j);
					}
					for (int i = 0; i < mRemainingLength; i++) {
						if (byte_temp[i] == commonReaderProtocol.getDelimiter()) {
							count++;
							if (count == 1) {
								start = i;
								continue;
							}
							if (count == 2) {
								end = i;
								break;
							}
						}
					}
                    //未找到第二个7e  那么视为半包
					if (count == 1) {
						SLog.i("ResendReaderImpl------半包 mRemainingLength：" + mRemainingLength);
						SLog.i("ResendReaderImpl------半包:" + HexStringUtils.toHexString(byte_temp));
						break;
					}
					//头尾都找到  则视为整包
					if (count == 2) {
						//需要解析的字节流
						byte[] byte_item_temp = new byte[end - start + 1];
						SLog.i("ResendReaderImpl------byte_item_temp长度:" + byte_item_temp.length + "start:" + start + "end：" + end);
						System.arraycopy(byte_temp, start, byte_item_temp, 0, end - start + 1);
//                                currentLength = currentLength - byte_temp.length;
						//拿到未转义数据
						SLog.i("ResendReaderImpl------转义之前:" + HexStringUtils.toHexString(byte_temp));
//                                CarMasterApplication.getInstance().getSocketLogClient().writeMsg("转义前:" + HexStringUtils.toHexString(buffer));
						//拿到转义数据
						byte[] transBuffer = commonReaderProtocol.getmIByteEscape().decodeBytes(byte_item_temp, 0, byte_item_temp.length);
						SLog.i("ResendReaderImpl------转义之后:" + transBuffer.length);
						byte[] byte_item = new byte[transBuffer.length];
						System.arraycopy(transBuffer, 0, byte_item, 0, byte_item.length);
//                                LogUtil.d(TAG, "SocketClient,ReceiveThread------item:" + HexStringUtils.toHexString(byte_item));
//                                CarMasterApplication.getInstance().getSocketLogClient().writeMsg("输出结果:" + HexStringUtils.toHexString(byte_item));
						//校验码是否正确  判断是否是一条正确的原始信息
						int check = byte_item[byte_item.length - 2];
						int calculateCheck = BitOperator.getCheckSum4JT808(byte_item, 1, byte_item.length - 2);
						SLog.i("ResendReaderImpl------currentLength:" + mRemainingLength);
						//如果校验码不正确  说明该包有问题  需要丢弃
						SLog.i("ResendReaderImpl------check:" + check + "calcheck:" + calculateCheck);
						if (check != calculateCheck) {
							mRemainingLength = mRemainingLength - byte_item_temp.length;
							byteList.subList(0, end + 1).clear();
							continue;
						}
						SLog.i("ResendReaderImpl:" + HexStringUtils.toHexString(byte_item));
						//前面的验证都通过  说明是一个完整的包  可以发送给界面
//                                currentLength = currentLength - byte_item_temp.length;
						mRemainingLength = mRemainingLength - end - 1;
//                                System.arraycopy(byte_temp, byte_item_temp.length, buffer, 0, currentLength);
						SLog.i("ResendReaderImpl------end:" + end);
						byteList.subList(0, end + 1).clear();
						SLog.i( "ResendReaderImpl------currentLength:" + mRemainingLength);
						SLog.i( "ResendReaderImpl------\n完整的消息：" + HexStringUtils.toHexString(byte_item));
						//收到服务器过来的消息，就发送回调
						OriginalData originalData = new OriginalData();
						originalData.setHeadBytes(BitOperator.splitBytes(byte_item,0,commonReaderProtocol.getHeaderLength() - 1));
						originalData.setBodyBytes(BitOperator.splitBytes(byte_item,commonReaderProtocol.getHeaderLength(),byte_item.length));
						mStateSender.sendBroadcast(IOAction.ACTION_READ_COMPLETE, originalData);
						SLog.i("ResendReaderImpl------bytelistsize:" + byteList.size() + "byte_temp:" + byte_temp.length + "currentLength:" + mRemainingLength);
						SLog.i("ResendReaderImpl------一轮解析完成");
					}
				}
			}
		} catch (Exception e) {
			ReadException readException = new ReadException(e);
			throw readException;
		}

	}

	/**
	 * 将缓存区内容保存到byteList缓存队列中
	 *
	 * @throws IOException
	 */
	private void readPackageFromChannel() throws IOException {
		int len = mInputStream.available();
		mInputStream.read(buffer, 0, len);
		mRemainingLength += len;
		//每次buffer缓存区读取的内容放到解析缓存区中
		for (int k = 0; k < len; k++) {
			byteList.add(buffer[k]);
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
	private void readHeaderFromChannel(ByteBuffer headBuf, int readLength, boolean isDelimiter, int delimiter) throws IOException {
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
		if (!isDelimiter) {
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
