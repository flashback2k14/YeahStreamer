package com.yeahdev.yeahstreamer.utils;

import android.content.Context;
import android.widget.Toast;


public class ToastWrapper {

    private Context mContext;

    public ToastWrapper(Context context) {
        this.mContext = context;
    }

    public void showShort(String msg) {
        Toast.makeText(this.mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public void showLong(String msg) {
        Toast.makeText(this.mContext, msg, Toast.LENGTH_LONG).show();
    }
}
