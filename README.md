# EasySocket
在这里要首先感谢[OkSocket](https://github.com/xuuhaoo/OkSocket)的代码作者xuuhaoo，我是在他的基础上完成的EasySocket。

### EasySocket Introduce
EasySocket保留了OkSocket的所有功能，并且根据我在实际应用开发过程中的经验增加以及修改了：</br>
  #### 1.补发机制</br>
  #### 2.自定义解析方式</br>
  #### 3.报文解析的优化</br>
  #### 4.实用的字节处理工具</br>
  
### 如何使用

引入EasySocket

```groovy
由于网络问题，稍后我会发布lib到jcenter仓库
大家可以先下载lib，作为工程引入。
```


#### 在需要使用的地方创建socket通道
```
EasySocketOptions mOkOptions = new EasySocketOptions.Builder()
			.setIsResendMode(true)
			.setReaderProtocol(new CommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER,
				true, APIConfig.pkg_delimiter, 13, true, new DefaultByteEscape()))
			.setReconnectionManager(new DefaultReconnectManager())
			.setConnectTimeoutSecond(10)
			.setSocketResendHandler(defaultResendActionHandler)
			.setCallbackThreadModeToken(new EasySocketOptions.ThreadModeToken() {
				@Override
				public void handleCallbackEvent(ActionDispatcher.ActionRunnable runnable) {
					handler.post(runnable);
				}
			})
			.build();
		mManager = EasySocket.open(mInfo, mOkOptions);
 ```
####创建通道后建立连接
```
mManager.connect();
```
####发送消息
```
mManager.send(data)
```

详细的使用方法可以看EasySocket下的使用实例，里面包含客户端和服务器端代码，可以在android设备上实际测试。

### EasySocket配置属性说明
```
                        new EasySocketOptions.Builder()
			//重发模式下 被标记为需要重发的信息会不断重发，直至收到服务器应答根据判断条件移除
			.setIsResendMode(true)
			//定义框架用什么模式进行解析 可以通过分隔符+包长，也可以首位分隔符，当然在oksocket中的只使用包长也可以
//			    .setReaderProtocol(new CommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_PACKAGE_LENGTH,
//					13,true,APIConfig.pkg_delimiter,true))
			.setReaderProtocol(new CommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER,
				true, APIConfig.pkg_delimiter, 13, true, new DefaultByteEscape()))
			//重连管理器
			.setReconnectionManager(new DefaultReconnectManager())
			//设置连接的超时时间
			.setConnectTimeoutSecond(10)
			//对数据发送失败后的自定义补发处理  根据服务器应答移除补发内容的操作就是在这里
			.setSocketResendHandler(defaultResendActionHandler)
			//设置消息的回调线程
			.setCallbackThreadModeToken(new EasySocketOptions.ThreadModeToken() {
				@Override
				public void handleCallbackEvent(ActionDispatcher.ActionRunnable runnable) {
					handler.post(runnable);
				}
			})
			//设置框架的io读写模式  分为单工模式和双工模式 推荐双工
			.setIOThreadMode(EasySocketOptions.IOThreadMode.DUPLEX)
			//设置框架读取的消息的单条缓存大小 单位MB
			.setMaxReadDataMB(5)
			//设置框架心跳间隔 单位毫秒
			.setPulseFrequency(3000)
			//设置框架在多少次心跳没收到后判定连接失效
			.setPulseFeedLoseTimes(3)
		    //设置框架缓存区每次读取的长度 单位byte
			.setReadPackageBytes(50)
			//发送给服务器时单个数据包的总长度
			.setWritePackageBytes(100)
			.build();
 ```

### License

```
   Copyright [2020] [LiTtleBayReal]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

