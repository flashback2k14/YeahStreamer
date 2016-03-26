package com.yeahdev.yeahstreamer.utils;


public class Constants {

    private Constants() {}

    public static final String ACTION_PLAY = "com.yeahdev.yeahstreamer.action.PLAY";
    public static final String ACTION_PAUSE = "com.yeahdev.yeahstreamer.action.PAUSE";
    public static final String ACTION_STOP = "com.yeahdev.yeahstreamer.action.STOP";

    public static final int NOTIFICATION_ID = 1234;

    public static final String EXTRA_STATION_NAME = "STATION_NAME";
    public static final String EXTRA_STATION_URI = "STATION_URI";
    public static final String EXTRA_BUFFER_PROGRESS = "EXTRA_BUFFER_PROGRESS";
    public static final String EXTRA_ERROR_TYPE = "EXTRA_ERROR_TYPE";
    public static final String EXTRA_ERROR_MSG = "EXTRA_ERROR_MSG";
    public static final String EXTRA_NETWORK_CHECK = "EXTRA_NETWORK_CHECK";
    public static final String EXTRA_NETWORK_FLAG = "EXTRA_NETWORK_FLAG";
    public static final String EXTRA_NETWORK_MSG = "EXTRA_NETWORK_MSG";


    public static final String ACTION_PLAYBACK_STARTED = "com.yeahdev.yeahstreamer.action.PLAYBACK_STARTED";
    public static final String ACTION_PLAYBACK_PAUSED = "com.yeahdev.yeahstreamer.action.PLAYBACK_PAUSED";
    public static final String ACTION_PLAYBACK_STOPPED = "com.yeahdev.yeahstreamer.action.PLAYBACK_STOPPED";
    public static final String ACTION_PLAYBACK_PROGRESS = "com.yeahdev.yeahstreamer.action.PLAYBACK_PROGRESS";

    public static final String CURRENT_PLAYBACK_STATE = "PLAYBACK";
    public static final String CURRENT_PLAYBACK_VOLUME = "VOLUME";

    public static final String CURRENT_PLAYING_STATE = "CURRENT_PLAYING_STATE";
    public static final String CURRENT_RADIO_STATION_ICON = "CURRENT_RADIO_STATION_ICON";
    public static final String CURRENT_RADIO_STATION_NAME = "CURRENT_RADIO_STATION_NAME";
    public static final String CURRENT_RADIO_STATION_URL = "CURRENT_RADIO_STATION_URL";
    public static final String CURRENT_RADIO_STATION_KEY = "CURRENT_RADIO_STATION_KEY";

    public static final String FIREBASE_REF = "https://yeah-streamer.firebaseio.com/";
    public static final String FIREBASE_ROUTE_USER = "users";
    public static final String FIREBASE_ROUTE_RADIOSTATION = "radiostations";

    public static final String FIREBASE_UPDATE_NAME = "name";
    public static final String FIREBASE_UPDATE_URL = "url";

    public static final String M3U_FILE = "m3u";
    public static final String PLS_FILE = "pls";
}
