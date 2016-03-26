package com.yeahdev.yeahstreamer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.Util;


public class NetworkChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // info message
        StringBuilder msg = new StringBuilder();
        boolean isAvailable;
        // info headline
        msg.append("YEAH! Streamer - INFO").append("\n");
        // check network connection
        if (Util.isInternetAvailable(context)) {
            msg.append("Network Connection is available!");
            isAvailable = true;
        } else {
            msg.append("No Network Connection available!");
            isAvailable = false;
        }
        // Send error message as intent to main activity
        Intent i = new Intent();
        i.setAction(Constants.EXTRA_NETWORK_CHECK);
        i.putExtra(Constants.EXTRA_NETWORK_FLAG, isAvailable);
        i.putExtra(Constants.EXTRA_NETWORK_MSG, msg.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }
}
