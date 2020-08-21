package com.littlebayreal.easysocketlib.client.delegate.io;

import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.client.pojo.OriginalData;
import com.littlebayreal.easysocketlib.exceptions.ReadException;
import com.littlebayreal.easysocketlib.interfaces.action.IOAction;
import com.littlebayreal.easysocketlib.interfaces.io.IIOCoreOptions;
import com.littlebayreal.easysocketlib.protocol.CommonReaderProtocol;
import com.littlebayreal.easysocketlib.util.BitOperator;
import com.littlebayreal.easysocketlib.util.HexStringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ResendReaderImpl extends AbsReader {
	//残留的数据
	private ByteBuffer mRemainingBuf;
	private byte[] buffer;
	@Override
	public void setOption(IIOCoreOptions option) {
		super.setOption(option);
		SLog.i("getMaxReadDataMB:"+ option.getMaxReadDataMB());
		buffer = new byte[option.getMaxReadDataMB()*1024*1024];
		mRemainingBuf = ByteBuffer.allocate(buffer.length);
	}
    public ResendReaderImpl(String threadName){
		super(threadName);
	}
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
					SLog.w(mThreadName +" mRemainingBuf array:"+ HexStringUtils.toHexString(mRemainingBuf.array()));
					int length = 0;
					//有开启分隔符 要先找分隔符索引
					if (commonReaderProtocol.isDelimiter()) {
						SLog.w(mThreadName +" mRemainingBuf size:"+ mRemainingBuf.remaining());
						int start = findDelimiterIndex(mRemainingBuf.array(), delimiter);
						SLog.w(mThreadName +" find delimiter Index:"+ start);
						if (start <= -1) {
							//没找到正确的分隔符，记录全部清除
							mRemainingBuf = null;
							return;
						}
						//比较缓存区剩余字节和头协议长度最小
						length = Math.min(mRemainingBuf.remaining() - start, headerLength);
						headBuf.put(mRemainingBuf.array(), start, length);
					} else {
						length = Math.min(mRemainingBuf.remaining(), headerLength);
						headBuf.put(mRemainingBuf.array(), 0, length);
					}
					//如果剩余字节小于头字节长度定义 需要继续读取数据
					if (length < headerLength) {
						//there are no data left
						mRemainingBuf = null;
						//从通道读取剩余的字节
						readHeaderFromChannel(headBuf, headerLength - length, false, 0);
					} else {
						//标记到已解析的位置
						mRemainingBuf.position(headerLength);
					}
				} else {
					readHeaderFromChannel(headBuf, headBuf.capacity(), commonReaderProtocol.isDelimiter(), commonReaderProtocol.getDelimiter());
				}
				//头部解析成功后 才可以解析body
				headBuf.flip();
				SLog.i(mThreadName+" head length:"+ headBuf.remaining());
				if (headBuf.remaining() == commonReaderProtocol.getHeaderLength()) {
					originalData.setHeadBytes(headBuf.array());
					int bodyLength = commonReaderProtocol.getBodyLength(headBuf.array(),mOkOptions.getReadByteOrder());
					//整个包就分为头和身体两部分
					int packageLength = commonReaderProtocol.getHeaderLength() + bodyLength;
					if (SLog.isDebug()) {
						SLog.i(mThreadName+" read head: " + HexStringUtils.toHexString(headBuf.array()));
					}
					bodyLength = packageLength - commonReaderProtocol.getHeaderLength();
					if (SLog.isDebug()) {
						SLog.i(mThreadName+" need read body length: " + bodyLength);
					}
					if (bodyLength > 0) {
						//计算出来的报文不可超过单条协议最大限制
						if (packageLength > mOkOptions.getMaxReadDataMB()*1024*1024) {
							throw new ReadException("Need to follow the transmission protocol.\r\n" +
								"Please check the client/server code.\r\n" +
								"According to the packet header data in the transport protocol, the package length is " + bodyLength + " Bytes.\r\n" +
								"You need check your <ReaderProtocol> definition");
						}
						ByteBuffer byteBuffer = ByteBuffer.allocate(bodyLength);
						byteBuffer.order(mOkOptions.getReadByteOrder());

						if (mRemainingBuf != null) {
							int bodyStartPosition = mRemainingBuf.position();
							SLog.i(mThreadName+" bodyStartPosition:"+ bodyStartPosition);
							int length = Math.min(mRemainingBuf.remaining(), bodyLength);
							byteBuffer.put(mRemainingBuf.array(), bodyStartPosition, length);
							mRemainingBuf.position(bodyStartPosition + length);
							//如果报文长度满足后  还有剩余字节
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
//							//缓存区剩下的字节不够了
//							if (length < bodyLength){
//								//读取body
//								readBodyFromChannel(byteBuffer);
//
//							} else{
//								//解析后的内容从缓存区移除
//								ByteBuffer temp = ByteBuffer.allocate(mRemainingBuf.remaining());
//								temp.order(mOkOptions.getReadByteOrder());
//								temp.put(mRemainingBuf.array(), mRemainingBuf.position(), mRemainingBuf.remaining());
//								SLog.i(mThreadName+"temp:"+ HexStringUtils.toHexString(temp.array()));
//								mRemainingBuf = temp;
//							}
//						}else{
//							//没有缓存记录  直接读取body
//							readBodyFromChannel(byteBuffer);
						}
						//读取body
						readBodyFromChannel(byteBuffer);
						//拿到body内容  并且将解析好的报文对象回传
						//cause this time data from remaining buffer not from channel.
						originalData.setBodyBytes(byteBuffer.array());
//						mStateSender.sendBroadcast(IOAction.ACTION_READ_COMPLETE, originalData);

						//解析后的内容从缓存区移除
//						ByteBuffer temp = ByteBuffer.allocate(mRemainingBuf.remaining());
//						temp.order(mOkOptions.getReadByteOrder());
//						temp.put(mRemainingBuf.array(), mRemainingBuf.position(), mRemainingBuf.remaining());
//						SLog.i(mThreadName+"temp:"+ HexStringUtils.toHexString(temp.array()));
//						mRemainingBuf = temp;
//						return;
					}else if (bodyLength == 0){
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
					}else if (bodyLength < 0){
						throw new ReadException(
							"read body is wrong,this socket input stream is end of file read " + bodyLength + " ,that mean this socket is disconnected by server");
					}
				}
				SLog.i(mThreadName+" read success:"+ originalData.toString());
				mStateSender.sendBroadcast(IOAction.ACTION_READ_COMPLETE, originalData);
			} else if (commonReaderProtocol.getmResolveType() == CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER) {
				if (!commonReaderProtocol.isDelimiter() || commonReaderProtocol.getDelimiter() == -1)
					throw new RuntimeException("use read by delimiter，please set delimiter and open setting first");
				readPackageFromChannel();
                //需要处理的所有字节
				mRemainingBuf.flip();
				//缓存区长度至少要大于最短包长 才会开始解析
				while (mRemainingBuf.remaining() >= (commonReaderProtocol.getHeaderLength() + (commonReaderProtocol.isOpenCheck() ? 1 : 0) + 1)) {
//					SLog.i(mThreadName+" 进入循环处理字节数据1");
					int count = 0;
					int start = 0;
					//默认是接收信息的长度
					int end = 0;
					//需要处理的所有字节
					byte[] byte_temp = new byte[mRemainingBuf.remaining()];
					for (int j = 0; j < mRemainingBuf.remaining(); j++) {
						byte_temp[j] = mRemainingBuf.get(j);
					}
					for (int i = 0; i < byte_temp.length; i++) {
						if ((byte_temp[i]&0xff) == (commonReaderProtocol.getDelimiter())) {
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
						mRemainingBuf.clear();
						//将剩余未解析的字节放入缓存区
						mRemainingBuf.put(byte_temp);
						SLog.i(mThreadName + " ResendReaderImpl------半包:position:"+ mRemainingBuf.position() + "-----------limit:"+ mRemainingBuf.limit());
						break;
					}
					//头尾都找到  则视为整包
					if (count == 2) {
						//需要解析的字节流
						byte[] byte_item_temp = new byte[end - start + 1];
						SLog.i(mThreadName + " ResendReaderImpl------byte_item_temp长度:" + byte_item_temp.length + "start:" + start + "end：" + end);
						System.arraycopy(byte_temp, start, byte_item_temp, 0, end - start + 1);
						//拿到未转义数据
						SLog.i(mThreadName + " ResendReaderImpl------转义之前:" + byte_item_temp.length);
//						SLog.i(mThreadName + " ResendReaderImpl------转义之前:" + HexStringUtils.toHexString(byte_item_temp));
						//拿到转义数据
						byte[] transBuffer = commonReaderProtocol.getmIByteEscape().decodeBytes(byte_item_temp, 0, byte_item_temp.length);
						SLog.i(mThreadName + " ResendReaderImpl------转义之后:" + transBuffer.length);
						byte[] byte_item = new byte[transBuffer.length];
						System.arraycopy(transBuffer, 0, byte_item, 0, byte_item.length);
						//校验码是否正确  判断是否是一条正确的原始信息
//						int check = byte_item[byte_item.length - 2];
						int check = commonReaderProtocol.getmIByteCheck().getCheckInData(byte_item);
						int calculateCheck = 0;
						SLog.i(mThreadName + " ResendReaderImpl------calculateCheck:" + commonReaderProtocol.getmIByteCheck().calculateCheckNum(byte_item, commonReaderProtocol.getmIByteCheck().getCheckOffset(),
							commonReaderProtocol.getmIByteCheck().getCheckLength(byte_item)));
						if (commonReaderProtocol.isOpenCheck() && commonReaderProtocol.getmIByteCheck() != null)
							calculateCheck = commonReaderProtocol.getmIByteCheck().calculateCheckNum(byte_item, commonReaderProtocol.getmIByteCheck().getCheckOffset(),
								commonReaderProtocol.getmIByteCheck().getCheckLength(byte_item));
						//如果校验码不正确  说明该包有问题  需要丢弃
						SLog.i(mThreadName + " ResendReaderImpl------check:" + check + "calcheck:" + calculateCheck);
						if (commonReaderProtocol.isOpenCheck() && check != calculateCheck) {
							SLog.i(mThreadName + " ResendReaderImpl------校验位错误丢弃报文");
							mRemainingBuf.clear();
							mRemainingBuf.put(byte_temp,end + 1,byte_temp.length - end - 1);
							mRemainingBuf.flip();
							continue;
						}
						mRemainingBuf.clear();
						mRemainingBuf.put(byte_temp,end + 1,byte_temp.length - end - 1);
						SLog.i( mThreadName + " ResendReaderImpl------处理一条报文后截取currentLength:" + mRemainingBuf.position());
						mRemainingBuf.flip();
						SLog.i( mThreadName + " ResendReaderImpl------byte_item：" + HexStringUtils.toHexString(byte_item));
						//收到服务器过来的消息，就发送回调
						OriginalData originalData = new OriginalData();
						originalData.setHeadBytes(BitOperator.splitBytes(byte_item,0,commonReaderProtocol.getHeaderLength(byte_item) - 1));
						originalData.setBodyBytes(BitOperator.splitBytes(byte_item,commonReaderProtocol.getHeaderLength(byte_item),byte_item.length - 1));
						mStateSender.sendBroadcast(IOAction.ACTION_READ_COMPLETE, originalData);
					}
				}
			}
		} catch (Exception e){
			SLog.e(mThreadName + "read error::"+ e.toString());
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
		if (len > 0) {
			mInputStream.read(buffer, 0, len);
			SLog.i(mThreadName+"readPackageFromChannel():len"+ len);

			//每次buffer缓存区读取的内容放到解析缓存区中

			SLog.i(mThreadName+"readPackageFromChannel():放入buffer前mRemainingBuf的position"+ mRemainingBuf.position() + "-------mRemain:"+ mRemainingBuf.remaining());
            mRemainingBuf.position(mRemainingBuf.position());
            mRemainingBuf.limit(mRemainingBuf.capacity());
			mRemainingBuf.put(buffer,0,len);

			SLog.i(mThreadName+"readPackageFromChannel():放入buffer后mRemainingBuf的position"+ mRemainingBuf.position()+ "--------mRemain:"+ mRemainingBuf.remaining());
		}
	}

	private void readBodyFromChannel(ByteBuffer byteBuffer) throws IOException {
		//缓存区还有缓存空间就一直读取
		while (byteBuffer.hasRemaining()) {
			try {
				byte[] bufArray = new byte[mOkOptions.getReadPackageBytes()];
				int len = mInputStream.read(bufArray);
				SLog.i(mThreadName + "readBodyFromChannel:" + len);
				SLog.i(mThreadName + "readBodyFromChannel:" + HexStringUtils.toHexString(bufArray));
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
			if (len > 0) {
				SLog.w(mThreadName + " read num from pipe"+ mInputStream.available());
				int value = mInputStream.read(bytes, size, readLength - size);
				if (value == -1) {
					throw new ReadException(
						"read head is wrong, this socket input stream is end of file read " + value + " ,that mean this socket is disconnected by server");
				}
				size += len;
			}
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
