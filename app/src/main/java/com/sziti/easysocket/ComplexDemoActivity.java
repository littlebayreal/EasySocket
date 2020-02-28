package com.sziti.easysocket;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sziti.easysocket.adapter.LogAdapter;
import com.sziti.easysocketlib.EasySocket;
import com.sziti.easysocketlib.base.ConnectionInfo;
import com.sziti.easysocketlib.base.EasySocketOptions;
import com.sziti.easysocketlib.client.delegate.action.SocketActionAdapter;
import com.sziti.easysocketlib.client.delegate.connection.NoneReconnect;
import com.sziti.easysocketlib.client.dispatcher.ActionDispatcher;
import com.sziti.easysocketlib.client.pojo.OriginalData;
import com.sziti.easysocketlib.exceptions.RedirectException;
import com.sziti.easysocketlib.interfaces.connection.IConnectionManager;
import com.sziti.easysocketlib.interfaces.send.IPulseSendable;
import com.sziti.easysocketlib.interfaces.send.ISendable;

import java.nio.charset.Charset;
import java.util.Arrays;


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

    private SocketActionAdapter adapter = new SocketActionAdapter() {

        @Override
        public void onSocketConnectionSuccess(ConnectionInfo info, String action) {
            logRece("连接成功(Connecting Successful)");
            //发送握手包
//            mManager.send(new HandShakeBean());
            mConnect.setText("DisConnect");
            initSwitch();
            //发送心跳
//            mManager.getPulseManager().setPulseSendable(new PulseBean());
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
            String str = new String(data.getBodyBytes(), Charset.forName("utf-8"));
            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            int cmd = jsonObject.get("cmd").getAsInt();
            if (cmd == 54) {//登陆成功
                String handshake = jsonObject.get("handshake").getAsString();
                logRece("握手成功! 握手信息(Handshake Success):" + handshake + ". 开始心跳(Start Heartbeat)..");
            } else if (cmd == 57) {//切换,重定向.(暂时无法演示,如有疑问请咨询github)
                String ip = jsonObject.get("data").getAsString().split(":")[0];
                int port = Integer.parseInt(jsonObject.get("data").getAsString().split(":")[1]);
                ConnectionInfo redirectInfo = new ConnectionInfo(ip, port);
                redirectInfo.setBackupInfo(mInfo.getBackupInfo());
                mManager.getReconnectionManager().addIgnoreException(RedirectException.class);
                mManager.disconnect(new RedirectException(redirectInfo));
            } else if (cmd == 14) {//心跳
                logRece("收到心跳,喂狗成功(Heartbeat Received,Feed the Dog)");
                mManager.getPulseManager().feed();
            } else {
                logRece(str);
            }
        }

        /**
         * 发送的回调
         * @param info
         * @param action
         * @param data   写出的数据{@link ISendable}
         */
        @Override
        public void onSocketWriteResponse(ConnectionInfo info, String action, ISendable data) {
            byte[] bytes = data.parse();
            bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
            String str = new String(bytes, Charset.forName("utf-8"));
            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            int cmd = jsonObject.get("cmd").getAsInt();
            switch (cmd) {
                case 54: {
                    String handshake = jsonObject.get("handshake").getAsString();
                    logSend("发送握手数据(Handshake Sending):" + handshake);
                    mManager.getPulseManager().pulse();
                    break;
                }
                default:
                    logSend(str);
            }
        }

        @Override
        public void onPulseSend(ConnectionInfo info, IPulseSendable data) {
            byte[] bytes = data.parse();
            bytes = Arrays.copyOfRange(bytes, 4, bytes.length);
            String str = new String(bytes, Charset.forName("utf-8"));
            JsonObject jsonObject = new JsonParser().parse(str).getAsJsonObject();
            int cmd = jsonObject.get("cmd").getAsInt();
            if (cmd == 14) {
                logSend("发送心跳包(Heartbeat Sending)");
            }
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
        mInfo = new ConnectionInfo("10.111.12.49", 8080);

        final Handler handler = new Handler(Looper.getMainLooper());
        EasySocketOptions.Builder builder = new EasySocketOptions.Builder();
        builder.setReconnectionManager(new NoneReconnect());
        builder.setCallbackThreadModeToken(new EasySocketOptions.ThreadModeToken() {
            @Override
            public void handleCallbackEvent(ActionDispatcher.ActionRunnable runnable) {
                handler.post(runnable);
            }
        });
        //打开通道
        mManager = EasySocket.open(mInfo).option(builder.build());
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
                if (mManager == null) {
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
                if (mManager == null) {
                    return;
                }
                mManager.getPulseManager().trigger();
            }
        });
    }

    private void logSend(String log) {
//        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
//        mSendLogAdapter.getDataList().add(0, logBean);
        mSendLogAdapter.notifyDataSetChanged();
    }

    private void logRece(String log) {
//        LogBean logBean = new LogBean(System.currentTimeMillis(), log);
//        mReceLogAdapter.getDataList().add(0, logBean);
        mReceLogAdapter.notifyDataSetChanged();
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

