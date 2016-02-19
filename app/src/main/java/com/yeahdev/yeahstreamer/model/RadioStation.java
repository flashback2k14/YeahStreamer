package com.yeahdev.yeahstreamer.model;


public class RadioStation {
    private byte[] mIcon;
    private String mName;
    private String mUrl;


    public byte[] getIcon() {
        return mIcon;
    }
    public void setIcon(byte[] icon) {
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
