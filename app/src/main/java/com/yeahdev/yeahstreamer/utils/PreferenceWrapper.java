package com.yeahdev.yeahstreamer.utils;

import android.content.SharedPreferences;
import android.media.AudioManager;

import com.yeahdev.yeahstreamer.models.RadioStation;
import com.yeahdev.yeahstreamer.service.StreamService;


public class PreferenceWrapper {
    /**
     * private Member
     */
    private SharedPreferences mPreferences;

    /**
     * Constructor
     * @param preferences - SharedPreferences
     */
    public PreferenceWrapper(SharedPreferences preferences) {
        this.mPreferences = preferences;
    }

    /**
     * BEGIN SETTER METHODS
     */
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
    public void setPlaybackStateService(int playbackState) {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putInt(Constants.CURRENT_PLAYBACK_STATE, playbackState);
        editor.apply();
    }
    public void setPlaybackVolume(AudioManager audioManager) {
        // current User Volume
        int currentVolume =
                audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) :
                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // save User Volume
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.putInt(Constants.CURRENT_PLAYBACK_VOLUME, currentVolume);
        editor.apply();
    }
    /**
     * END SETTER METHODS
     */

    /**
     * BEGIN GETTER METHODS
     */
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
    public int getPlaybackStateService() {
        if (this.mPreferences.contains(Constants.CURRENT_PLAYBACK_STATE)) {
            return this.mPreferences.getInt(Constants.CURRENT_PLAYBACK_STATE, StreamService.IS_STOPPED);
        }
        return StreamService.IS_STOPPED;
    }
    public int getPlaybackVolume() {
        if (this.mPreferences.contains(Constants.CURRENT_PLAYBACK_VOLUME)) {
            return this.mPreferences.getInt(Constants.CURRENT_PLAYBACK_VOLUME, 5);
        }
        return 5;
    }
    /**
     * END GETTER METHODS
     */

    /**
     * BEGIN RESET METHODS
     */
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
    /**
     * END RESET METHODS
     */
}
