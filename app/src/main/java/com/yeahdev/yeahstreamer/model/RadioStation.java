package com.yeahdev.yeahstreamer.model;


public class RadioStation {
    private String mIcon;
    private String mName;
    private String mUrl;

    public RadioStation() {}
    public RadioStation(String icon, String name, String url) {
        this.mIcon = icon;
        this.mName = name;
        this.mUrl = url;
    }

    public String getIcon() {
        return mIcon;
    }
    public void setIcon(String icon) {
        this.mIcon = icon;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        this.mName = name;
    }

    public String getUrl() {
        return mUrl;
    }
    public void setUrl(String url) {
        this.mUrl = url;
    }
}
