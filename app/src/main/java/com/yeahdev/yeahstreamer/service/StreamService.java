package com.yeahdev.yeahstreamer.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.NotificationWrapper;
import com.yeahdev.yeahstreamer.utils.PreferenceWrapper;

import java.io.IOException;


public class StreamService extends Service implements
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener {

    public static final int IS_PLAYING = 1;
    public static final int IS_PAUSED = 2;
    public static final int IS_PAUSED_ON_FOCUS_CHANGED = 3;
    public static final int IS_STOPPED = 4;

    private static final String LOG_TAG = StreamService.class.getSimpleName();

    private String mStationName;
    private String mDataSource;
    private int mInstanceCounter;

    private NotificationWrapper mNotificationWrapper;
    private PreferenceWrapper mPreferenceWrapper;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;


    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationWrapper = new NotificationWrapper(getApplicationContext(), this);
        mPreferenceWrapper = new PreferenceWrapper(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
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
                startPlayback();
                break;

            case Constants.ACTION_PAUSE:
                pausePlayback(false);
                break;

            case Constants.ACTION_STOP:
                stopPlayback();
                break;

            default:
                break;
        }

        return START_REDELIVER_INTENT;
    }

    /**
     * PLAYER ACTIONS
     */
    private void startPlayback() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            releaseMediaPlayer();
        }
        if (mDataSource != null && requestFocus()) {
            initMediaPlayer();
        }

        mPreferenceWrapper.setPlaybackStateService(StreamService.IS_PLAYING);
        mPreferenceWrapper.setPlaybackVolume(mAudioManager);

        mNotificationWrapper.setRadioStationName(mStationName + " - Playing");
        mNotificationWrapper.buildNotification(mNotificationWrapper.generateAction(R.drawable.ic_pause_24dp, "Pause", Constants.ACTION_PAUSE));

        mInstanceCounter++;

        Intent i = new Intent();
        i.setAction(Constants.ACTION_PLAYBACK_STARTED);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);
    }

    private void pausePlayback(boolean onAudioFocusChanged) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }

        if (onAudioFocusChanged) {
            mPreferenceWrapper.setPlaybackStateService(StreamService.IS_PAUSED_ON_FOCUS_CHANGED);
        } else {
            mPreferenceWrapper.setPlaybackStateService(StreamService.IS_PAUSED);
        }
        mPreferenceWrapper.setPlaybackVolume(mAudioManager);

        mNotificationWrapper.setRadioStationName(mStationName + " - Paused");
        mNotificationWrapper.buildNotification(mNotificationWrapper.generateAction(R.drawable.ic_play_arrow_24dp, "Play", Constants.ACTION_PLAY));

        mInstanceCounter = 0;

        Intent i = new Intent();
        i.setAction(Constants.ACTION_PLAYBACK_PAUSED);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);
    }

    private void stopPlayback() {
        releaseMediaPlayer();

        mDataSource = null;

        mPreferenceWrapper.setPlaybackStateService(StreamService.IS_STOPPED);
        mPreferenceWrapper.setPlaybackVolume(mAudioManager);

        mInstanceCounter = 0;
        stopForeground(true);

        Intent i = new Intent();
        i.setAction(Constants.ACTION_PLAYBACK_STOPPED);
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);
    }

    /**
     * UTIL
     */
    private boolean requestFocus() {
        return mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
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
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mPreferenceWrapper.getPlaybackVolume(), 0);
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
                switch (mPreferenceWrapper.getPlaybackStateService()) {
                    case StreamService.IS_PLAYING:
                        mMediaPlayer.setVolume(1.0f, 1.0f);
                        break;
                    case StreamService.IS_PAUSED_ON_FOCUS_CHANGED:
                        startPlayback();
                        break;
                    default:
                        break;
                }
                break;
            // loss of audio focus of unknown duration
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mPreferenceWrapper.getPlaybackStateService() == StreamService.IS_PLAYING) {
                    pausePlayback(true);
                }
                break;
            // transient loss of audio focus
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mPreferenceWrapper.getPlaybackStateService() == StreamService.IS_PLAYING) {
                    pausePlayback(true);
                }
                break;
            // temporary external request of audio focus
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mPreferenceWrapper.getPlaybackStateService() == StreamService.IS_PLAYING) {
                    mMediaPlayer.setVolume(0.2f, 0.2f);
                }
                break;
            default:
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
        // error message
        StringBuilder msg = new StringBuilder();
        // error headline
        msg.append("YEAH! Streamer - ERROR").append("\n");
        // what is the error
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(LOG_TAG, "Unknown media playback error");
                msg.append("Unknown media playback error.");
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e(LOG_TAG, "Connection to server lost");
                msg.append("Connection to server lost.");
                break;
            default:
                Log.e(LOG_TAG, "Generic audio playback error");
                msg.append("Generic audio playback error.");
                break;
        }
        // extra error message
        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.e(LOG_TAG, "IO media error.");
                msg.append("IO media error.");
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                Log.e(LOG_TAG, "Malformed media.");
                msg.append("Malformed media.");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Log.e(LOG_TAG, "Unsupported content type");
                msg.append("Unsupported content type.");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Log.e(LOG_TAG, "Media timeout");
                msg.append("Media timeout.");
                break;
            default:
                Log.e(LOG_TAG, "Other case of media playback error");
                msg.append("Other case of media playback error.");
                break;
        }
        // Send error message as intent to main activity
        Intent i = new Intent();
        i.setAction(Constants.EXTRA_ERROR_TYPE);
        i.putExtra(Constants.EXTRA_ERROR_MSG, msg.toString());
        LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);
        // reset media player
        mp.reset();
        // return value
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // what is the info
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
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                Log.i(LOG_TAG, "New metadata available");
                break;
            default:
                Log.i(LOG_TAG, "other case of media info");
                break;
        }
        // return value
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (percent % 5 == 0) {
            Log.v(LOG_TAG, "Buffering: " + percent);
            Intent i = new Intent();
            i.setAction(Constants.ACTION_PLAYBACK_PROGRESS);
            i.putExtra(Constants.EXTRA_BUFFER_PROGRESS, percent);
            LocalBroadcastManager.getInstance(this.getApplication()).sendBroadcast(i);
        }
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
        // reset data source
        mDataSource = null;
        // set state
        mPreferenceWrapper.setPlaybackStateService(StreamService.IS_STOPPED);
        // release media player
        releaseMediaPlayer();
        // stop service
        stopForeground(true);
    }
}
