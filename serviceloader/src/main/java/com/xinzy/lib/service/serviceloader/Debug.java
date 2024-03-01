package com.xinzy.lib.service.serviceloader;

import android.util.Log;

class Debug {
    static boolean isDebug = true;

    static void d(String tag, String msg) {
        if (!isDebug) return;

        Log.d(tag, msg);
    }

    static void i(String tag, String msg) {
        if (!isDebug) return;

        Log.i(tag, msg);
    }
}
