package com.yeahdev.yeahstreamer.utils;

import android.content.SharedPreferences;
import com.yeahdev.yeahstreamer.models.RadioStation;


public class PreferenceWrapper {

    private SharedPreferences mPreferences;

    public PreferenceWrapper(SharedPreferences preferences) {
        this.mPreferences = preferences;
    }

    public void setCurrentRadioStation(RadioStation radioStation) {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putString(Constants.CURRENT_RADIO_STATION_ICON, radioStation.getIcon());
        editor.putString(Constants.CURRENT_RADIO_STATION_NAME, radioStation.getName());
        editor.putString(Constants.CURRENT_RADIO_STATION_URL, radioStation.getUrl());
        editor.putString(Constants.CURRENT_RADIO_STATION_KEY, radioStation.getKey());
        editor.apply();
    }
    public void setPlaybackState(boolean playback) {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putBoolean(Constants.CURRENT_PLAYING_STATE, playback);
        editor.apply();
    }

    public RadioStation getCurrentRadioStation() {
        if (this.mPreferences.contains(Constants.CURRENT_RADIO_STATION_ICON) &&
            this.mPreferences.contains(Constants.CURRENT_RADIO_STATION_NAME) &&
            this.mPreferences.contains(Constants.CURRENT_RADIO_STATION_URL)  &&
            this.mPreferences.contains(Constants.CURRENT_RADIO_STATION_KEY)) {

            RadioStation rs = new RadioStation();

            rs.setIcon(this.mPreferences.getString(Constants.CURRENT_RADIO_STATION_ICON, ""));
            rs.setName(this.mPreferences.getString(Constants.CURRENT_RADIO_STATION_NAME, ""));
            rs.setUrl(this.mPreferences.getString(Constants.CURRENT_RADIO_STATION_URL, ""));
            rs.setKey(this.mPreferences.getString(Constants.CURRENT_RADIO_STATION_KEY, ""));

            return rs;
        } else {
            return null;
        }
    }
    public boolean getPlaybackState() {
        return this.mPreferences.contains(Constants.CURRENT_PLAYING_STATE) &&
                this.mPreferences.getBoolean(Constants.CURRENT_PLAYING_STATE, false);
    }

    public void resetCurrentRadioStation() {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.remove(Constants.CURRENT_RADIO_STATION_ICON);
        editor.remove(Constants.CURRENT_RADIO_STATION_NAME);
        editor.remove(Constants.CURRENT_RADIO_STATION_URL);
        editor.remove(Constants.CURRENT_RADIO_STATION_KEY);
        editor.apply();
    }
    public void resetPlaybackState() {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.remove(Constants.CURRENT_PLAYING_STATE);
        editor.apply();
    }
}
