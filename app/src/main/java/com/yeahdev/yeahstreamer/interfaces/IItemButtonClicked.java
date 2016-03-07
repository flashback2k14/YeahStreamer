package com.yeahdev.yeahstreamer.interfaces;

import com.yeahdev.yeahstreamer.models.RadioStation;


public interface IItemButtonClicked {
    void onPlayRadioStation(RadioStation radioStation);
    void onEditRadioStation(RadioStation radioStation);
    void onDeleteRadioStation(RadioStation radioStation);
}
