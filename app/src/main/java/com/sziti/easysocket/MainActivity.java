package com.sziti.easysocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sziti.easysocket.data.MsgDataBean;
import com.sziti.easysocket.data.PulseBean;
import com.sziti.easysocket.data.Register;
import com.sziti.easysocket.data.TextData;
import com.sziti.easysocketlib.EasySocket;
import com.sziti.easysocketlib.SLog;
import com.sziti.easysocketlib.base.EasySocketOptions;
import com.sziti.easysocketlib.client.delegate.io.DefaultByteEscape;
import com.sziti.easysocketlib.client.delegate.protocol.Jt808ProtocolHeader;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.config.APIConfig;
import com.sziti.easysocketlib.interfaces.protocol.IHeaderProtocol;
import com.sziti.easysocketlib.interfaces.send.ISendable;
import com.sziti.easysocketlib.protocol.CommonReaderProtocol;
import com.sziti.easysocketlib.server.action.ServerActionAdapter;
import com.sziti.easysocketlib.server.impl.EasyServerOptions;
import com.sziti.easysocketlib.server.interfaces.IClient;
import com.sziti.easysocketlib.server.interfaces.IClientIOCallback;
import com.sziti.easysocketlib.server.interfaces.IClientPool;
import com.sziti.easysocketlib.server.interfaces.IServerManager;
import com.sziti.easysocketlib.server.interfaces.IServerShutdown;
import com.sziti.easysocketlib.util.BitOperator;
import com.sziti.easysocketlib.util.HexStringUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IClientIOCallback {
	private Button mSimpleBtn;

	private Button mComplexBtn;

	private Button mServerBtn;

	private Button mAdminBtn;

	private IServerManager mServerManager;

	private TextView mIPTv;

	private int mPort = 8080;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);
		mSimpleBtn = findViewById(R.id.btn1);
		mComplexBtn = findViewById(R.id.btn2);
		mServerBtn = findViewById(R.id.btn3);
		mIPTv = findViewById(R.id.ip);

		EasySocketOptions.setIsDebug(true);
		EasyServerOptions.setIsDebug(true);
		SLog.setIsDebug(true);

		mSimpleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SimpleDemoActivity.class);
				startActivity(intent);
			}
		});
		mComplexBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, ComplexDemoActivity.class);
				startActivity(intent);
			}
		});
		mServerManager = EasySocket.server(mPort).registerReceiver(new ServerActionAdapter() {
			@Override
			public void onServerListening(int serverPort) {
				Log.i("ServerCallback", Thread.currentThread().getName() + " onServerListening,serverPort:" + serverPort);
				flushServerText();
			}

			@Override
			public void onClientConnected(IClient client, int serverPort, IClientPool clientPool) {
				Log.i("ServerCallback", Thread.currentThread().getName() + " onClientConnected,serverPort:" + serverPort + "--ClientNums:" + clientPool.size() + "--ClientTag:" + client.getUniqueTag());
				//为客户端注册读写监听
				client.registerReceiver(MainActivity.this);
			}

			@Override
			public void onClientDisconnected(IClient client, int serverPort, IClientPool clientPool) {
				Log.i("ServerCallback", Thread.currentThread().getName() + " onClientDisconnected,serverPort:" + serverPort + "--ClientNums:" + clientPool.size() + "--ClientTag:" + client.getUniqueTag());
				client.unRegisterReceiver(MainActivity.this);
			}

			@Override
			public void onServerWillBeShutdown(int serverPort, IServerShutdown shutdown, IClientPool clientPool, Throwable throwable) {
				Log.i("ServerCallback", Thread.currentThread().getName() + " onServerWillBeShutdown,serverPort:" + serverPort + "--ClientNums:" + clientPool
					.size());
				shutdown.shutdown();
			}

			@Override
			public void onServerAlreadyShutdown(int serverPort) {
				Log.i("ServerCallback", Thread.currentThread().getName() + " onServerAlreadyShutdown,serverPort:" + serverPort);
				flushServerText();
			}
		});
		mIPTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData myClip = ClipData.newPlainText("ip", mIPTv.getText().toString().substring(5));
				myClipboard.setPrimaryClip(myClip);
				Toast.makeText(MainActivity.this, "复制到剪切板", Toast.LENGTH_LONG).show();
			}
		});

		mServerBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mServerManager.isLive()) {
//					mServerManager.listen();
					//启动服务器并开始监听
//					mServerManager.listen(new EasyServerOptions.Builder().setReaderProtocol(new CommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_PACKAGE_LENGTH,
//						13,true, APIConfig.pkg_delimiter,true)).build());

					mServerManager.listen(new EasyServerOptions.Builder().setReaderProtocol(new CommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER,
						true,APIConfig.pkg_delimiter,13,true,new DefaultByteEscape())).build());
				} else {
					mServerManager.shutdown();
				}
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		flushServerText();
		mIPTv.setText("当前IP(Local Device IP):" + getIPAddress());
	}

	//刷新服务器提示信息
	private void flushServerText() {
		if (mServerManager.isLive()) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					mServerBtn.setText(mPort + "服务器关闭(Local Server Demo in " + mPort + " Stop)");
				}
			});
		} else {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					mServerBtn.setText(mPort + "服务器启动(Local Server Demo in " + mPort + " Start)");
				}
			});
		}
	}
	public String getIPAddress() {
		NetworkInfo info = ((ConnectivityManager)
			getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
				try {
					//Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
					for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
						NetworkInterface intf = en.nextElement();
						for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
							InetAddress inetAddress = enumIpAddr.nextElement();
							if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
								return inetAddress.getHostAddress();
							}
						}
					}
				} catch (SocketException e) {
					e.printStackTrace();
				}

			} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
				WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				int ipAddress = wifiInfo.getIpAddress();
				if (ipAddress == 0) return "未连接wifi";
				return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
					+ (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
			}
		} else {
			//当前无网络连接,请在设置中打开网络
			return "当前无网络连接,请在设置中打开网络";
		}
		return "IP获取失败";
	}

	@Override
	public void onClientRead(OriginalData originalData, IClient client, IClientPool<IClient, String> clientPool) {
		SLog.i("server收到的消息:"+ HexStringUtils.toHexString(originalData.getHeadBytes()));
		int msg_id = BitOperator.byteToInteger(BitOperator.splitBytes(originalData.getHeadBytes(),1,2));
		switch (msg_id){
			//注册成功
			case 0x0100:
				byte[] body = BitOperator.splitBytes(originalData.getBodyBytes(),0,originalData.getBodyBytes().length - 3);
				Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " 握手信息:" + new String(body,APIConfig.string_charset));

				//发送注册报文
				Register register = new Register("注册");
				IHeaderProtocol iHeaderProtocol = new Jt808ProtocolHeader.Builder()
					.setMsgId(0x0100)
					.setTerminalPhone("15850101933").build();
				register.setHeaderProtocol(iHeaderProtocol);
				register.setSerialNum(BitOperator.byteToInteger(BitOperator.splitBytes(originalData.getHeadBytes(),
					11,12)));
				client.send(register);
				break;
				//接收文字消息报文
			case 0x0200:
				byte[] msg = BitOperator.splitBytes(originalData.getBodyBytes(),0,originalData.getBodyBytes().length - 3);
				Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " 文字信息:" + new String(msg,APIConfig.string_charset));

				//发送内容报文
				TextData textData = new TextData(new String(msg,APIConfig.string_charset));
				IHeaderProtocol ih = new Jt808ProtocolHeader.Builder()
					.setMsgId(0x0200)
					.setTerminalPhone("15850101933").build();
				textData.setHeaderProtocol(ih);
				textData.setSerialNum(BitOperator.byteToInteger(BitOperator.splitBytes(originalData.getHeadBytes(),
					11,12)));
				client.send(textData);
				break;
			case 0x0300:
				byte[] msg0 = BitOperator.splitBytes(originalData.getBodyBytes(),0,originalData.getBodyBytes().length - 3);
				Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " 文字信息:" + new String(msg0,APIConfig.string_charset));

				//发送内容报文
				PulseBean pulseBean = new PulseBean();
				IHeaderProtocol ih0 = new Jt808ProtocolHeader.Builder()
					.setMsgId(0x0300)
					.setTerminalPhone("15850101933").build();
				pulseBean.setHeaderProtocol(ih0);
				pulseBean.setSerialNum(BitOperator.byteToInteger(BitOperator.splitBytes(originalData.getHeadBytes(),
					11,12)));
				client.send(pulseBean);
				break;
		}
//		String str = new String(originalData.getBodyBytes(), Charset.forName("utf-8"));
//		JsonObject jsonObject = null;
//		try {
//			jsonObject = new JsonParser().parse(str).getAsJsonObject();
//			int cmd = jsonObject.get("cmd").getAsInt();
//			if (cmd == 54) {//登陆成功22
//				String handshake = jsonObject.get("handshake").getAsString();
//				Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " 握手信息:" + handshake);
//			} else if (cmd == 14) {//心跳
//				Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " 收到心跳");
//			} else {
//				Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " " + str);
//			}
//		} catch (Exception e) {
//			Log.i("onClientIOServer", Thread.currentThread().getName() + " 接收到:" + client.getHostIp() + " " + str);
//		}
//		MsgDataBean msgDataBean = new MsgDataBean(str);
//		clientPool.sendToAll(msgDataBean);
	}

	@Override
	public void onClientWrite(ISendable sendable, IClient client, IClientPool<IClient, String> clientPool) {
		byte[] bytes = sendable.parse();
		Log.i("MainActivity",HexStringUtils.toHexString(bytes));
//		bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
//		String str = new String(bytes, Charset.forName("utf-8"));
//		JsonObject jsonObject = null;
		try {
//			jsonObject = new JsonParser().parse(str).getAsJsonObject();
//			int cmd = jsonObject.get("cmd").getAsInt();
//			switch (cmd) {
//				case 54: {
//					String handshake = jsonObject.get("handshake").getAsString();
//					Log.i("onClientIOServer", Thread.currentThread().getName() + " 发送给:" + client.getHostIp() + " 握手数据:" + handshake);
//					break;
//				}
//				default:
//					Log.i("onClientIOServer", Thread.currentThread().getName() + " 发送给:" + client.getHostIp() + " " + str);
//			}
		} catch (Exception e) {
//			Log.i("onClientIOServer", Thread.currentThread().getName() + " 发送给:" + client.getHostIp() + " " + str);
		}
	}
}
