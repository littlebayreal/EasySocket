package com.littlebayreal.easysocket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.littlebayreal.easysocket.adapter.LogAdapter;
import com.littlebayreal.easysocket.data.LogBean;
import com.littlebayreal.easysocket.data.Register;
import com.littlebayreal.easysocket.data.TextData;
import com.littlebayreal.easysocketlib.EasySocket;
import com.littlebayreal.easysocketlib.base.ConnectionInfo;
import com.littlebayreal.easysocketlib.base.EasySocketOptions;
import com.littlebayreal.easysocketlib.client.delegate.action.SocketActionAdapter;
import com.littlebayreal.easysocketlib.client.delegate.connection.NoneReconnect;
import com.littlebayreal.easysocketlib.client.delegate.io.DefaultByteEscape;
import com.littlebayreal.easysocketlib.client.delegate.protocol.Jt808ProtocolHeader;
import com.littlebayreal.easysocketlib.client.dispatcher.ActionDispatcher;
import com.littlebayreal.easysocketlib.client.dispatcher.DefaultResendActionHandler;
import com.littlebayreal.easysocketlib.client.pojo.OriginalData;
import com.littlebayreal.easysocketlib.config.APIConfig;
import com.littlebayreal.easysocketlib.interfaces.connection.IConnectionManager;
import com.littlebayreal.easysocketlib.interfaces.protocol.IHeaderProtocol;
import com.littlebayreal.easysocketlib.interfaces.send.IPulseSendable;
import com.littlebayreal.easysocketlib.interfaces.send.ISendable;
import com.littlebayreal.easysocketlib.protocol.CommonReaderProtocol;
import com.littlebayreal.easysocketlib.protocol.CustomCommonReaderProtocol;
import com.littlebayreal.easysocketlib.util.BitOperator;
import com.littlebayreal.easysocketlib.util.HexStringUtils;
import com.littlebayreal.easysocketlib.util.SerialNumGen;

import java.nio.charset.Charset;

/**
 * Created by Tony on 2017/10/24.
 */

public class SimpleDemoActivity extends AppCompatActivity {
    private ConnectionInfo mInfo;

    private Button mConnect;

    private EditText mIPET;
    private EditText mPortET;
    private IConnectionManager mManager;
    private EditText mSendET;
    private EasySocketOptions mOkOptions;
    private Button mClearLog;
    private Button mSendBtn;

    private RecyclerView mSendList;
    private RecyclerView mReceList;

    private LogAdapter mSendLogAdapter = new LogAdapter();
    private LogAdapter mReceLogAdapter = new LogAdapter();

    private DefaultResendActionHandler defaultResendActionHandler = new DefaultResendActionHandler();
    private SocketActionAdapter adapter = new SocketActionAdapter() {
        @Override
        public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
//            mManager.send(new HandShakeBean());
			//发送注册报文
			Register register = new Register("注册");
			IHeaderProtocol iHeaderProtocol = new Jt808ProtocolHeader.Builder()
				.setMsgId(0x0100)
				.setTerminalPhone("10512909090").build();
			register.setHeaderProtocol(iHeaderProtocol);
			register.setSerialNum(SerialNumGen.getInstance().getSerialNum());
			mManager.send(register);
			logSend("连接成功(Connecting Success!!!)");
            mConnect.setText("DisConnect");
            mIPET.setEnabled(false);
            mPortET.setEnabled(false);
        }

        @Override
        public void onSocketDisconnection(ConnectionInfo info, String action, Exception e) {
            if (e != null) {
                logSend("异常断开(Disconnected with exception):" + e.getMessage());
            } else {
                logSend("正常断开(Disconnect Manually)");
            }
            mConnect.setText("Connect");
            mIPET.setEnabled(true);
            mPortET.setEnabled(true);
        }

        @Override
        public void onSocketConnectionFailed(ConnectionInfo info, String action, Exception e) {
            logSend("连接失败(Connecting Failed)");
            mConnect.setText("Connect");
            mIPET.setEnabled(true);
            mPortET.setEnabled(true);
        }

        @Override
        public void onSocketReadResponse(ConnectionInfo info, String action, OriginalData data) {
//            String str = new String(data.getBodyBytes(), Charset.forName("utf-8"));
//            logRece(str);
			logRece(data);
        }

        @Override
        public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
//            String str = new String(data.parse(), Charset.forName("utf-8"));
            logSend(HexStringUtils.toHexString(data.parse()));
        }

        @Override
        public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
            String str = new String(data.parse(), Charset.forName("utf-8"));
            logSend(str);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        findViews();
        initData();
        setListener();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    private void findViews() {
        mSendList = findViewById(R.id.send_list);
        mReceList = findViewById(R.id.rece_list);
        mIPET = findViewById(R.id.ip);
        mPortET = findViewById(R.id.port);
        mClearLog = findViewById(R.id.clear_log);
        mConnect = findViewById(R.id.connect);
        mSendET = findViewById(R.id.send_et);
        mSendBtn = findViewById(R.id.send_btn);
    }

    private void initData() {
        LinearLayoutManager manager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSendList.setLayoutManager(manager1);
        mSendList.setAdapter(mSendLogAdapter);

        LinearLayoutManager manager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mReceList.setLayoutManager(manager2);
        mReceList.setAdapter(mReceLogAdapter);

        initManager();
    }

    //打开连接通道
    private void initManager() {
        final Handler handler = new Handler();
        mInfo = new ConnectionInfo(mIPET.getText().toString(), Integer.parseInt(mPortET.getText().toString()));
        mOkOptions = new EasySocketOptions.Builder()
			    .setIsResendMode(true)
			    .setReaderProtocol(new CustomCommonReaderProtocol(CommonReaderProtocol.PROTOCOL_RESOLUTION_BY_DELIMITER,
					true, APIConfig.pkg_delimiter, 13, true, new DefaultByteEscape()))
                .setReconnectionManager(new NoneReconnect())
                .setConnectTimeoutSecond(30)
                .setCallbackThreadModeToken(new EasySocketOptions.ThreadModeToken() {
                    @Override
                    public void handleCallbackEvent(ActionDispatcher.ActionRunnable runnable) {
                        handler.post(runnable);
                    }
                })
			    .setSocketResendHandler(defaultResendActionHandler)
                .build();
		mManager = EasySocket.open(mInfo,mOkOptions);
//        mManager = EasySocket.open(mInfo).option(mOkOptions);
        mManager.registerReceiver(adapter);
    }


    private void setListener() {
        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mManager == null) {
                    return;
                }
                if (!mManager.isConnect()) {
                    initManager();
                    mManager.connect();
                    mIPET.setEnabled(false);
                    mPortET.setEnabled(false);
                } else {
                    mConnect.setText("Disconnecting");
                    mManager.disconnect();
                }
            }
        });
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mManager == null) {
                    return;
                }
//                if (!mManager.isConnect()) {
//                    Toast.makeText(getApplicationContext(), "Unconnected", LENGTH_SHORT).show();
//                } else {
                    String msg = mSendET.getText().toString();
                    if (TextUtils.isEmpty(msg.trim())) {
                        return;
                    }
//                    MsgDataBean msgDataBean = new MsgDataBean(msg);

					TextData textData = new TextData(msg);
					IHeaderProtocol iHeaderProtocol = new Jt808ProtocolHeader.Builder()
						.setMsgId(0x0200)
						.setTerminalPhone("15850101933").build();
					textData.setSerialNum(SerialNumGen.getInstance().getSerialNum());
					textData.setHeaderProtocol(iHeaderProtocol);
					textData.setReSend(true);
					mManager.send(textData);
                    mSendET.setText("");
//                }
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
    }

    private void logSend(final String log) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            LogBean logBean = new LogBean(System.currentTimeMillis(), log);
            mSendLogAdapter.getDataList().add(0, logBean);
            mSendLogAdapter.notifyDataSetChanged();
        } else {
            final String threadName = Thread.currentThread().getName();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    logSend(threadName + " 线程打印(In Thread):" + log);
                }
            });
        }
    }
    private void logRece(final OriginalData originalData){
		final int msg_id = BitOperator.byteToInteger(BitOperator.splitBytes(originalData.getHeadBytes(),1,2));
		Log.i("MainActivity","msg_id:"+ msg_id);
		if (Looper.myLooper() == Looper.getMainLooper()) {
			LogBean logBean = null;
			switch (msg_id){
				case 0x0100:
					 logBean = new LogBean(System.currentTimeMillis(), HexStringUtils.toHexString(originalData.getHeadBytes())
						+" "+HexStringUtils.toHexString(originalData.getBodyBytes()));
					break;
				case 0x0200:
					logBean = new LogBean(System.currentTimeMillis(), new String(BitOperator.splitBytes(originalData.getBodyBytes(),0,
						originalData.getBodyBytes().length - 3),APIConfig.string_charset));
					Log.i("SimpleDemoActivity",logBean.toString());
					break;
			}
			mReceLogAdapter.getDataList().add(0, logBean);
			mReceLogAdapter.notifyDataSetChanged();
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
    private void logRece(final String log) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            LogBean logBean = new LogBean(System.currentTimeMillis(), log);
            mReceLogAdapter.getDataList().add(0, logBean);
            mReceLogAdapter.notifyDataSetChanged();
        } else {
            final String threadName = Thread.currentThread().getName();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    logRece(threadName + " 线程打印(In Thread):" + log);
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
