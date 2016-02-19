package com.yeahdev.yeahstreamer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.util.Constants;

import java.io.IOException;


public class StreamService extends Service implements
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    private static final String LOG_TAG = StreamService.class.getSimpleName();

    private String mStationName;
    private String mDataSource;

    private int mInstanceCounter;
    private boolean mPlayback;

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;


    @Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getAction() == null) {
            return -1;
        }

        switch (intent.getAction()) {

            case Constants.ACTION_PLAY:

                if (intent.hasExtra(Constants.EXTRA_STATION_NAME)) {
                    mStationName = intent.getStringExtra(Constants.EXTRA_STATION_NAME);
                }
                if (intent.hasExtra(Constants.EXTRA_STATION_URI)) {
                    mDataSource = intent.getStringExtra(Constants.EXTRA_STATION_URI);
                }

                mPlayback = true;
                preparePlayback();
                buildNotification(generateAction(R.drawable.ic_pause_24dp, "Pause", Constants.ACTION_PAUSE));
                mInstanceCounter++;

                break;

            case Constants.ACTION_PAUSE:
                pausePlayback();
                buildNotification(generateAction(R.drawable.ic_play_arrow_24dp, "Play", Constants.ACTION_PLAY));
                mInstanceCounter = 0;

                break;

            case Constants.ACTION_STOP:
                mPlayback = false;
                finishPlayback();
                mInstanceCounter = 0;

                break;

            default:
                break;
        }

        return START_STICKY;
    }

    /**
     * PLAYER ACTIONS
     */
    private void preparePlayback() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            releaseMediaPlayer();
        }

        if (mDataSource != null && requestFocus()) {
            initMediaPlayer();
        }

        savePlaybackState();

        Intent i = new Intent();
        i.setAction(Constants.ACTION_PLAYBACK_STARTED);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);
    }

    private void pausePlayback() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }

        savePlaybackState();

        Intent i = new Intent();
        i.setAction(Constants.ACTION_PLAYBACK_PAUSED);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);
    }

    private void finishPlayback() {
        releaseMediaPlayer();
        savePlaybackState();

        Intent i = new Intent();
        i.setAction(Constants.ACTION_PLAYBACK_STOPPED);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);

        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIFICATION_ID);
    }

    /**
     * NOTIFICATION
     */
    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), StreamService.class).setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(NotificationCompat.Action action) {
        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_radio_24dp);
        builder.setContentTitle("Yeah! Streamer");
        builder.setContentText(mStationName);
        builder.setStyle(style);

        builder.addAction(action);
        builder.addAction(generateAction(R.drawable.ic_stop_24dp, "Stop", Constants.ACTION_STOP));
        style.setShowActionsInCompactView(0, 1);

        builder.setAutoCancel(false);
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFICATION_ID, notification);
    }

    /**
     * UTIL
     */
    private boolean requestFocus() {
        int result = mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void savePlaybackState () {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplication());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.PLAYBACK, mPlayback);
        editor.apply();
    }

    /**
     * MEDIA PLAYER
     */
    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);

        try {
            mMediaPlayer.setDataSource(mDataSource);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    /**
     * AUDIO MANAGER LISTENER
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            // gain of audio focus of unknown duration
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPlayback) {
                    if (mMediaPlayer == null) {
                        initMediaPlayer();
                    } else if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            // loss of audio focus of unknown duration
            case AudioManager.AUDIOFOCUS_LOSS:
                finishPlayback();
                break;
            // transient loss of audio focus
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (!mPlayback && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    finishPlayback();
                }
                else if (mPlayback && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                break;
            // temporary external request of audio focus
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    /**
     * MEDIA PLAYER LISTENER
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mInstanceCounter == 1) {
            mp.start();
            mInstanceCounter--;
        } else {
            releaseMediaPlayer();
            mInstanceCounter--;
            if (mInstanceCounter >= 0) {
                initMediaPlayer();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mMediaPlayer.reset();
        mInstanceCounter++;
        initMediaPlayer();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(LOG_TAG, "Unknown media playback error");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e(LOG_TAG, "Connection to server lost");
                break;
            default:
                Log.e(LOG_TAG, "Generic audio playback error");
                break;
        }

        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.e(LOG_TAG, "IO media error.");
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                Log.e(LOG_TAG, "Malformed media.");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Log.e(LOG_TAG, "Unsupported content type");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Log.e(LOG_TAG, "Media timeout");
                break;
            default:
                Log.e(LOG_TAG, "Other case of media playback error");
                break;
        }

        mp.reset();

        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_INFO_UNKNOWN:
                Log.i(LOG_TAG, "Unknown media info");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                Log.i(LOG_TAG, "Buffering started");
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                Log.i(LOG_TAG, "Buffering finished");
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE: // case never selected
                Log.i(LOG_TAG, "New metadata available");
                break;
            default:
                Log.i(LOG_TAG, "other case of media info");
                break;
        }

        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.v(LOG_TAG, "Buffering: " + percent);
    }

    /**
     * SERVICE
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // save state
        mPlayback = false;
        savePlaybackState();

        // retrieve notification system service and cancel notification
        NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.NOTIFICATION_ID);
    }
}
