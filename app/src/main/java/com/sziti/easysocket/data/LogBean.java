package com.sziti.easysocket.data;

import java.text.SimpleDateFormat;

public class LogBean {
    public String mTime;
    public String mLog;
    public String mWho;

    public LogBean(long time, String log) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        mTime = format.format(time);
        mLog = log;
    }

	@Override
	public String toString() {
		return "LogBean{" +
			"mTime='" + mTime + '\'' +
			", mLog='" + mLog + '\'' +
			", mWho='" + mWho + '\'' +
			'}';
	}
}
