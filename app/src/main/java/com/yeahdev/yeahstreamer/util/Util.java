package com.yeahdev.yeahstreamer.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.model.RadioStation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutionException;


public class Util {

    private Util() {}

    public static byte[] createPlaceholderIcon(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public static String getUsername(Object email) {
        String mail = (String) email;
        return mail.split("@")[0];
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        if (name.isEmpty()) {
            return "";
        } else {
            return name.substring(name.lastIndexOf(".") + 1);
        }
    }

    public static RadioStation getRadioStation(Context context, String name, String url) {
        String ext = Util.getFileExtension(new File(url));
        String urlSave = "";

        switch (ext) {
            case Constants.M3U_FILE:
                try {
                    urlSave = new UrlLoader().execute(ext, url).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                break;

            case Constants.PLS_FILE:
                try {
                    urlSave = new UrlLoader().execute(ext, url).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                break;

            default:
                urlSave = url;
        }

        RadioStation radioStation = new RadioStation();
        radioStation.setIcon(Base64.encodeToString(Util.createPlaceholderIcon(context),Base64.DEFAULT));
        radioStation.setName(name);
        radioStation.setUrl(urlSave);

        return radioStation;
    }
}
