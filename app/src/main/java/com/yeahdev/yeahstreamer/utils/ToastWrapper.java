package com.yeahdev.yeahstreamer.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;


public class ToastWrapper {

    private Context mContext;
    private Toast mToast;

    public ToastWrapper(Context context) {
        this.mContext = context;
        this.mToast = new Toast(context);
    }

    public void showShort(String msg) {
        if (this.mToast != null) {
            mToast.cancel();
        }
        this.mToast = Toast.makeText(this.mContext, msg, Toast.LENGTH_SHORT);
        this.mToast.show();
    }

    public void showLong(String msg) {
        if (this.mToast != null) {
            mToast.cancel();
        }
        this.mToast = Toast.makeText(this.mContext, msg, Toast.LENGTH_LONG);
        this.mToast.show();
    }
}
