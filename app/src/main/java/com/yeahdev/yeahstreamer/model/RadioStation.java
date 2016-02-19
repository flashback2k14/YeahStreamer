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
        /*StringBuilder sb = new StringBuilder();
        sb.append("#EXTM3U\n\n");
        sb.append("#EXTINF:-1,");
        sb.append(mName);
        sb.append("\n");
        sb.append(url);
        sb.append("\n");
        this.mUrl = sb.toString();*/
        this.mUrl = url;
    }
}
