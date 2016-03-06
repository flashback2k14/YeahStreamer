package com.yeahdev.yeahstreamer.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class UrlLoader extends AsyncTask<String, Void, String> {
    String result;
    boolean isM3u;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        result = "";
        isM3u = false;
    }

    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        isM3u = params[0].equals("m3u");

        try {
            URL url = new URL(params[1]);
            urlConnection = (HttpURLConnection) url.openConnection();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        if (isM3u) {
                            if (line.startsWith("#")) {
                                Log.v(UrlLoader.class.getSimpleName(), "Reader - Metadata .m3u");
                            } else if (line.length() > 0) {
                                if (line.startsWith("http://")) {
                                    result = line;
                                }
                            }
                        } else {
                            if (line.startsWith("[playlist]")) {
                                Log.v(UrlLoader.class.getSimpleName(), "Reader - Metadata .pls");
                            } else if (line.length() > 0) {
                                if (line.startsWith("File1")) {
                                    result = line.substring(6);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e(UrlLoader.class.getSimpleName(), "Response Error!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return result;
    }
}
