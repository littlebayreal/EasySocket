package com.littlebayreal.easysocket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.littlebayreal.easysocket.adapter.LogAdapter;
import com.littlebayreal.easysocket.data.LogBean;
import com.littlebayreal.easysocket.data.PulseBean;
import com.littlebayreal.easysocket.data.PulseRequestBean;
import com.littlebayreal.easysocket.data.Register;
import com.littlebayreal.easysocketlib.EasySocket;
import com.littlebayreal.easysocketlib.SLog;
import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.base.EasySocketOptions;
import com.littlebayreal.easysocketlib.client.delegate.action.SocketActionAdapter;
import com.littlebayreal.easysocketlib.client.delegate.connection.DefaultReconnectManager;
import com.littlebayreal.easysocketlib.client.delegate.connection.NoneReconnect;
import com.littlebayreal.easysocketlib.client.delegate.io.DefaultByteEscape;
import com.littlebayreal.easysocketlib.client.delegate.protocol.Jt808ProtocolHeader;
import com.littlebayreal.easysocketlib.client.dispatcher.ActionDispatcher;
import com.littlebayreal.easysocketlib.client.dispatcher.DefaultResendActionHandler;
import com.littlebayreal.easysocketlib.client.pojo.OriginalData;
import com.littlebayreal.easysocketlib.config.APIConfig;
import com.littlebayreal.easysocketlib.exceptions.RedirectException;
import com.littlebayreal.easysocketlib.interfaces.connection.IConnectionManager;
import com.littlebayreal.easysocketlib.interfaces.protocol.IHeaderProtocol;
import com.littlebayreal.easysocketlib.interfaces.send.IPulseSendable;
import com.littlebayreal.easysocketlib.interfaces.send.ISendable;
import com.littlebayreal.easysocketlib.protocol.CommonReaderProtocol;
import com.littlebayreal.easysocketlib.protocol.CustomCommonReaderProtocol;
import com.littlebayreal.easysocketlib.util.BitOperator;
import com.littlebayreal.easysocketlib.util.HexStringUtils;
import com.littlebayreal.easysocketlib.util.SerialNumGen;

public class ComplexDemoActivity extends AppCompatActivity {

	private ConnectionInfo mInfo;

	private Button mConnect;
	private IConnectionManager mManager;
	private EditText mIPET;
	private EditText mPortET;
	private Button mRedirect;
	private EditText mFrequencyET;
	private Button mSetFrequency;
	private Button mMenualPulse;
	private Button mClearLog;
	private SwitchCompat mReconnectSwitch;

	private RecyclerView mSendList;
	private RecyclerView mReceList;

	private LogAdapter mSendLogAdapter = new LogAdapter();
	private LogAdapter mReceLogAdapter = new LogAdapter();
	private DefaultResendActionHandler defaultResendActionHandler = new DefaultResendActionHandler();
	private SocketActionAdapter adapter = new SocketActionAdapter() {

		@Override
		public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
			logRece("连接成功(Connecting Successful)");
			//发送握手包
//            mManager.send(new HandShakeBean());
			//发送注册报文
//			Register register = new Register("注册");
//			IHeaderProtocol iHeaderProtocol = new Jt808ProtocolHeader.Builder()
//				.setMsgId(0x0100)
//				.setTerminalPhone("10512999999").build();
//			register.setHeaderProtocol(iHeaderProtocol);
//			register.setSerialNum(SerialNumGen.getInstance().getSerialNum());
//			mManager.send(register);
			mConnect.setText("DisConnect");
//			initSwitch();

			//设置心跳
			//设置心跳  首次触发可以在鉴权成功之后 调用mIconnectManager.getPulseManager().pulse();
			PulseRequestBean pulseRequestData = new PulseRequestBean();
			IHeaderProtocol iHeaderProtocol = new Jt808ProtocolHeader.Builder()
				.setMsgId(0x0002)
				.setTerminalPhone("10512999999").build();
			pulseRequestData.setSerialNum(SerialNumGen.getInstance().getSerialNum());
			pulseRequestData.setHeaderProtocol(iHeaderProtocol);
			mManager.getPulseManager().setPulseSendable(pulseRequestData);

//			PulseBean pulseBean = new PulseBean();
//			iHeaderProtocol = new Jt808ProtocolHeader.Builder()
//				.setMsgId(0x0300)
//				.setTerminalPhone("15850101933").build();
//			pulseBean.setSerialNum(SerialNumGen.getInstance().getSerialNum());
//			pulseBean.setHeaderProtocol(iHeaderProtocol);
//			mManager.getPulseManager().setPulseSendable(pulseBean);
			mIPET.setEnabled(true);
			mPortET.setEnabled(true);
		}

		private void initSwitch() {
			EasySocketOptions okSocketOptions = mManager.getOption();
			mReconnectSwitch.setChecked(!(okSocketOptions.getReconnectionManager() instanceof NoneReconnect));
		}

		@Override
		public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
			if (e != null) {
				if (e instanceof RedirectException) {
					logSend("正在重定向连接(Redirect Connecting)...");
					mManager.switchConnectionInfo(((RedirectException) e).redirectInfo);
					mManager.connect();
					mIPET.setEnabled(true);
					mPortET.setEnabled(true);
				} else {
					logSend("异常断开(Disconnected with exception):" + e.getMessage());
					mIPET.setEnabled(false);
					mPortET.setEnabled(false);
				}
			} else {
				logSend("正常断开(Disconnect Manually)");
				mIPET.setEnabled(false);
				mPortET.setEnabled(false);
			}
			mConnect.setText("Connect");
		}

		@Override
		public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
			logSend("连接失败(Connecting Failed)");
			mConnect.setText("Connect");
			mIPET.setEnabled(false);
			mPortET.setEnabled(false);
		}

		@Override
		public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
			logRece(data);
//            String str = new String(data.getBodyBytes(), Charset.forName("utf-8"));
//            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
//            int cmd = jsonObject.get("cmd").getAsInt();
//            if (cmd == 54) {//登陆成功
//                String handshake = jsonObject.get("handshake").getAsString();
//                logRece("握手成功! 握手信息(Handshake Success):" + handshake + ". 开始心跳(Start Heartbeat)..");
//            } else if (cmd == 57) {//切换,重定向.(暂时无法演示,如有疑问请咨询github)
//                String ip = jsonObject.get("data").getAsString().split(":")[0];
//                int port = Integer.parseInt(jsonObject.get("data").getAsString().split(":")[1]);
//                ConnectionInfo redirectInfo = new ConnectionInfo(ip, port);
//                redirectInfo.setBackupInfo(mInfo.getBackupInfo());
//                mManager.getReconnectionManager().addIgnoreException(RedirectException.class);
//                mManager.disconnect(new RedirectException(redirectInfo));
//            } else if (cmd == 14) {//心跳
//                logRece("收到心跳,喂狗成功(Heartbeat Received,Feed the Dog)");
//                mManager.getPulseManager().feed();
//            } else {
//                logRece(str);
//            }
		}

		/**
		 * 发送的回调
		 * @param info
		 * @param action
		 * @param data   写出的数据{@link ISendable}
		 */
		@Override
		public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
			logSend(HexStringUtils.toHexString(data.parse()));
			byte[] bytes = data.parse();
//            bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
//            String str = new String(bytes, Charset.forName("utf-8"));
//            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
//            int cmd = jsonObject.get("cmd").getAsInt();
//            switch (cmd) {
//                case 54: {
//                    String handshake = jsonObject.get("handshake").getAsString();
//                    logSend("发送握手数据(Handshake Sending):" + handshake);
//                    mManager.getPulseManager().pulse();
//                    break;
//                }
//                default:
//                    logSend(str);
//            }
		}

		@Override
		public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
//            byte[] bytes = data.parse();
//            bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
//            String str = new String(bytes, Charset.forName("utf-8"));
//            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
//            int cmd = jsonObject.get("cmd").getAsInt();
//            if (cmd == 14) {
//			logSend("发送心跳包(Heartbeat Sending):" + HexStringUtils.toHexString(data.parse()));
//            }
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complex);
		findViews();
		initData();
		setListener();
	}

	private void findViews() {
		mSendList = findViewById(R.id.send_list);
		mReceList = findViewById(R.id.rece_list);
		mClearLog = findViewById(R.id.clear_log);
		mSetFrequency = findViewById(R.id.set_pulse_frequency);
		mFrequencyET = findViewById(R.id.pulse_frequency);
		mConnect = findViewById(R.id.connect);
		mIPET = findViewById(R.id.ip);
		mPortET = findViewById(R.id.port);
		mRedirect = findViewById(R.id.redirect);
		mMenualPulse = findViewById(R.id.manual_pulse);
		mReconnectSwitch = findViewById(R.id.switch_reconnect);
	}

	private void initData() {
		mIPET.setEnabled(false);
		mPortET.setEnabled(false);

		LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mSendList.setLayoutManager(manager1);
		mSendList.setAdapter(mSendLogAdapter);

		LinearLayoutManager manager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mReceList.setLayoutManager(manager2);
		mReceList.setAdapter(mReceLogAdapter);
		//设置连接信息
		mInfo = new ConnectionInfo("192.168.128.2", 10002);

		final Handler handler = new Handler(Looper.getMainLooper());
//        EasySocketOptions.Builder builder = new EasySocketOptions.Builder();
//        builder.setReconnectionManager(new NoneReconnect());
//        builder.setCallbackThreadModeToken(new EasySocketOptions.ThreadModeToken() {
//            @Override
//            public void handleCallbackEvent(ActionDispatcher.ActionRunnable runnable) {
//                handler.post(runnable);
//            }
//        });
//        //打开通道
//        mManager = EasySocket.open(mInfo).option(builder.build());
		EasySocketOptions mOkOptions = new EasySocketOptions.Builder()
			//重发模式下 被标记为需要重发的信息会不断重发，直至收到服务器应答根据判断条件移除
			.setIsResendMode(true)
			//定义框架用什么模式进行解析 可以通过分隔符+包长，也可以首位分隔符，当然在oksocket中的只使用包长也可以
//			    .setReaderProtocol(new CommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_PACKAGE_LENGTH,
//					13,true,APIConfig.pkg_delimiter,true))
			.setReaderProtocol(new CustomCommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER,
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
		mManager = EasySocket.open(mInfo, mOkOptions);
	}

	private void setListener() {
		//注册对socket的监听服务
		mManager.registerReceiver(adapter);

		mReconnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					if (!(mManager.getReconnectionManager() instanceof NoneReconnect)) {
						mManager.option(new EasySocketOptions.Builder(mManager.getOption()).setReconnectionManager(new NoneReconnect()).build());
						logSend("关闭重连管理器(Turn Off The Reconnection Manager)");
					}
				} else {
					if (mManager.getReconnectionManager() instanceof NoneReconnect) {
						mManager.option(new EasySocketOptions.Builder(mManager.getOption()).setReconnectionManager(EasySocketOptions.getDefault().getReconnectionManager()).build());
						logSend("打开重连管理器(Turn On The Reconnection Manager)");
					}
				}
			}
		});
		/**
		 * 点击连接
		 */
		mConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mManager == null) {
					return;
				}
				if (!mManager.isConnect()) {
					mManager.connect();
				} else {
					mConnect.setText("DisConnecting");
					mManager.disconnect();
				}
			}
		});

		mClearLog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReceLogAdapter.getDataList().clear();
				mSendLogAdapter.getDataList().clear();
				mReceLogAdapter.notifyDataSetChanged();
				mSendLogAdapter.notifyDataSetChanged();
			}
		});

		mRedirect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mManager == null) {
					return;
				}
				String ip = mIPET.getText().toString();
				String portStr = mPortET.getText().toString();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("cmd", 57);
				jsonObject.addProperty("data", ip + ":" + portStr);
//                DefaultSendBean bean = new DefaultSendBean();
//                bean.setContent(new Gson().toJson(jsonObject));
//                mManager.send(bean);
			}
		});

		mSetFrequency.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mManager == null || mManager.getPulseManager() == null) {
					return;
				}
				String frequencyStr = mFrequencyET.getText().toString();
				long frequency = 0;
				try {
					frequency = Long.parseLong(frequencyStr);
					EasySocketOptions okOptions = new EasySocketOptions.Builder(mManager.getOption())
						.setPulseFrequency(frequency)
						.build();
					mManager.option(okOptions);
				} catch (NumberFormatException e) {

				}
			}
		});
		//触发心跳
		mMenualPulse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mManager == null || mManager.getPulseManager() == null) {
					return;
				}
//				mManager.getPulseManager().trigger();
				mManager.getPulseManager().pulse();
			}
		});
	}

	private void logSend(String log) {
		LogBean logBean = new LogBean(System.currentTimeMillis(), log);
		mSendLogAdapter.getDataList().add(0, logBean);
		mSendLogAdapter.notifyDataSetChanged();
	}

	private void logRece(String log) {
		LogBean logBean = new LogBean(System.currentTimeMillis(), log);
		mReceLogAdapter.getDataList().add(0, logBean);
		mReceLogAdapter.notifyDataSetChanged();
	}

	private void logRece(final OriginalData originalData) {
		final int msg_id = BitOperator.byteToInteger(BitOperator.splitBytes(originalData.getHeadBytes(), 1, 2));
		Log.i("ComplexDemoActivity", "msg_id:" + msg_id);
		if (Looper.myLooper() == Looper.getMainLooper()) {
			LogBean logBean = null;
			switch (msg_id) {
				case 0x0100:
					logBean = new LogBean(System.currentTimeMillis(), HexStringUtils.toHexString(originalData.getHeadBytes())
						+ " " + HexStringUtils.toHexString(originalData.getBodyBytes()));

					mManager.getPulseManager().pulse();
					break;
				case 0x0200:
					logBean = new LogBean(System.currentTimeMillis(), new String(BitOperator.splitBytes(originalData.getBodyBytes(), 0,
						originalData.getBodyBytes().length - 3), APIConfig.string_charset));
					Log.i("ComplexDemoActivity", logBean.toString());

					mReceLogAdapter.getDataList().add(0, logBean);
					mReceLogAdapter.notifyDataSetChanged();
					break;
				case 0x0300:
					logBean = new LogBean(System.currentTimeMillis(), new String(BitOperator.splitBytes(originalData.getBodyBytes(), 0,
						originalData.getBodyBytes().length - 3), APIConfig.string_charset));
					//喂狗
					mManager.getPulseManager().feed();
					break;
				case 0x8001:
					logBean = new LogBean(System.currentTimeMillis(), "接收心跳报文:" + HexStringUtils.toHexString(BitOperator.concatAll(originalData.getHeadBytes(),
						originalData.getBodyBytes())));
					//喂狗
					mManager.getPulseManager().feed();
					break;
				default:
					logBean = new LogBean(System.currentTimeMillis(), HexStringUtils.toHexString(BitOperator.concatAll(originalData.getHeadBytes(),
						originalData.getBodyBytes())));
					break;
			}
		} else {
			final String threadName = Thread.currentThread().getName();
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					logRece(originalData);
				}
			});
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mManager != null) {
			mManager.disconnect();
			mManager.unRegisterReceiver(adapter);
		}
	}

}

