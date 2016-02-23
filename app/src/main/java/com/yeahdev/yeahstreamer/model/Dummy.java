package com.yeahdev.yeahstreamer.model;

import android.content.Context;
import android.util.Base64;

import com.yeahdev.yeahstreamer.util.UrlLoader;
import com.yeahdev.yeahstreamer.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class Dummy {
    private Context mContext;
    private ArrayList<RadioStation> mCollection;

    public Dummy(Context context) {
        this.mContext = context;
        mCollection = new ArrayList<>();
    }

    public ArrayList<RadioStation> getCollection() {
        RadioStation radioStation = new RadioStation();
        radioStation.setIcon(Base64.encodeToString(Util.createPlaceholderIcon(mContext), Base64.DEFAULT));
        radioStation.setName("Radio Fritz");
        radioStation.setUrl("http://fritz.de/livemp3");

        RadioStation radioStation1 = new RadioStation();
        radioStation1.setIcon(Base64.encodeToString(Util.createPlaceholderIcon(mContext), Base64.DEFAULT));
        radioStation1.setName("MDR Info - m3u");
        String ext1 = Util.getFileExtension(new File("http://avw.mdr.de/livestreams/mdr_info_live_56.m3u"));
        String uri1 = "";
        try {
            uri1 = new UrlLoader().execute(ext1, "http://avw.mdr.de/livestreams/mdr_info_live_56.m3u").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        radioStation1.setUrl(uri1);

        RadioStation radioStation2 = new RadioStation();
        radioStation2.setIcon(Base64.encodeToString(Util.createPlaceholderIcon(mContext), Base64.DEFAULT));
        radioStation2.setName("MDR Info - pls");
        String ext2 = Util.getFileExtension(new File("http://avw.mdr.de/livestreams/mdr_info_live_56.pls"));
        String uri2 = "";
        try {
            uri2 = new UrlLoader().execute(ext2, "http://avw.mdr.de/livestreams/mdr_info_live_56.pls").get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        radioStation2.setUrl(uri2);

        mCollection.add(radioStation);
        mCollection.add(radioStation1);
        mCollection.add(radioStation2);

        return mCollection;
    }
}
