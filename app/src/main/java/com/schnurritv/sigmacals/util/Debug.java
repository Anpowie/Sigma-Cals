package com.schnurritv.sigmacals.util;

import android.util.Log;

public class Debug {

    public static void log(String msg) {
        Log.d("sexy", msg);
    }

    public static void log(int msg) {
        Log.d("sexy", String.valueOf(msg));
    }
    public static void log(float msg) {
        Log.d("sexy", String.valueOf(msg));
    }
    public static void log(boolean msg) {
        Log.d("sexy", String.valueOf(msg));
    }


    public static void error(String msg) {
        Log.d("error", String.valueOf(msg));
    }

}
