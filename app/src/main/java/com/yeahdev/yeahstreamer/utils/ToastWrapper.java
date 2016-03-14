package com.yeahdev.yeahstreamer.utils;

import android.content.Context;
import android.widget.Toast;


public class ToastWrapper {
    /**
     * private Member
     */
    private Context mContext;
    private Toast mToast;

    /**
     * Constructor
     * @param context - Android Context
     */
    public ToastWrapper(Context context) {
        this.mContext = context;
        this.mToast = new Toast(context);
    }

    /**
     * Show short Message to the user
     * @param msg - Message
     */
    public void showShort(String msg) {
        if (this.mToast != null) {
            mToast.cancel();
        }
        this.mToast = Toast.makeText(this.mContext, msg, Toast.LENGTH_SHORT);
        this.mToast.show();
    }

    /**
     * Show long Message to the user
     * @param msg - Message
     */
    public void showLong(String msg) {
        if (this.mToast != null) {
            mToast.cancel();
        }
        this.mToast = Toast.makeText(this.mContext, msg, Toast.LENGTH_LONG);
        this.mToast.show();
    }
}
