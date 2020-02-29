package com.littlebayreal.easysocketlib;

/**
 * Created by xuhao on 2017/6/9.
 * 后期可以和本地日志记录管理器绑定
 */

public class SLog {
    private static boolean isDebug;

    public static void setIsDebug(boolean isDebug) {
        SLog.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void e(String msg) {
        if (isDebug) {
            System.err.println("EasySocket, " + msg);
        }
    }

    public static void i(String msg) {
        if (isDebug) {
            System.out.println("EasySocket, " + msg);
        }
    }

    public static void w(String msg) {
        i(msg);
    }
}
