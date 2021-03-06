package com.yeahdev.yeahstreamer.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.activities.MainActivity;
import com.yeahdev.yeahstreamer.service.StreamService;


public class NotificationWrapper {
    /**
     * private Member
     */
    private Context mContext;
    private StreamService mService;
    private String mRadioStationName;

    /**
     * Constructor
     * @param context - Android Context
     * @param service - Stream Service
     */
    public NotificationWrapper(Context context, StreamService service) {
        this.mContext = context;
        this.mService = service;
    }

    /**
     * Set Radio Station to display on the notification
     * @param radioStationName - Radio Station Name
     */
    public void setRadioStationName(String radioStationName) {
        this.mRadioStationName = radioStationName;
    }

    /**
     * Create a new Notification Action
     * @param icon - Action Icon
     * @param title - Action Title
     * @param intentAction - Intent to handle with onclick
     * @return - NotificationCompat.Action
     */
    public NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(mContext, StreamService.class).setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    /**
     * Build Notification for Stream Service
     * @param action - Custom Action --> Play or Pause
     */
    public void buildNotification(NotificationCompat.Action action) {
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.drawable.ic_radio_24dp);
        builder.setContentTitle("Yeah! Streamer");
        builder.setContentText(mRadioStationName);
        builder.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        builder.setStyle(style);

        builder.addAction(action);
        builder.addAction(generateAction(R.drawable.ic_stop_24dp, "Stop", Constants.ACTION_STOP));
        style.setShowActionsInCompactView(0, 1);

        Intent openAppIntent = new Intent(mContext, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(mContext, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(openAppPendingIntent);

        builder.setAutoCancel(false);
        builder.setOngoing(true);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        mService.startForeground(Constants.NOTIFICATION_ID, notification);
    }
}
