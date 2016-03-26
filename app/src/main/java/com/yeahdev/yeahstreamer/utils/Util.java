package com.yeahdev.yeahstreamer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.models.RadioStation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.ExecutionException;


public class Util {

    private Util() {}

    /**
     * Create Placeholder Icon, because custom icons not implemented yet
     * @param context - Android Context
     * @return byte[]
     */
    public static byte[] createPlaceholderIcon(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Get Username from email address
     * @param email - Email Address
     * @return String
     */
    public static String getUsername(Object email) {
        String mail = (String) email;
        return mail.split("@")[0];
    }

    /**
     * Extract extension from file
     * @param file - File
     * @return String
     */
    public static String getFileExtension(File file) {
        String name = file.getName();
        if (name.isEmpty()) {
            return "";
        } else {
            return name.substring(name.lastIndexOf(".") + 1);
        }
    }

    /**
     * Create a new Radio Station
     * @param context - Android Context
     * @param name - Radio Station Name
     * @param url - Radio Station URL
     * @return RadioStation
     */
    public static RadioStation createRadioStation(Context context, String name, String url) {
        // get file extension
        String ext = Util.getFileExtension(new File(url));
        // url holder if url must extract from .m3u or .pls
        String urlSave = "";
        // check file extension
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
        // create Radio Station
        RadioStation radioStation = new RadioStation();
        radioStation.setIcon(Base64.encodeToString(Util.createPlaceholderIcon(context),Base64.DEFAULT));
        radioStation.setName(name);
        radioStation.setUrl(urlSave);
        // return Radio Station
        return radioStation;
    }

    /**
     * BEGIN NETWORK CHECK
     */
    /**
     * Check if Wifi or Mobile Data is available
     * @param context - Android Context
     * @return boolean
     */
    public static boolean isInternetAvailable(Context context) {
        return isSpecificConnectionAvailable(context, ConnectivityManager.TYPE_WIFI) ||
                isSpecificConnectionAvailable(context, ConnectivityManager.TYPE_MOBILE);
    }
    /**
     * Check specific Network availability
     * @param context - Android Context
     * @param type - ConnectivityManager Type
     * @return boolean
     */
    public static boolean isSpecificConnectionAvailable(Context context, int type) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == type;
    }
    /**
     * END NETWORK CHECK
     */
}
