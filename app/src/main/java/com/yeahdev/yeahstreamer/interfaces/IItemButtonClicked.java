package com.yeahdev.yeahstreamer.interfaces;

import com.yeahdev.yeahstreamer.models.RadioStation;


public interface IItemButtonClicked {
    void playRadioStation(RadioStation radioStation);
    void editRadioStation(RadioStation radioStation);
    void deleteRadioStation(RadioStation radioStation);
}
