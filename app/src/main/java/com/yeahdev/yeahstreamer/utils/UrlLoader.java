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
    /**
     * private Member
     */
    private String result;
    private boolean isM3u;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // init member
        result = "";
        isM3u = false;
    }

    @Override
    protected String doInBackground(String... params) {
        // declare url connection
        HttpURLConnection urlConnection = null;
        // check if the first given parameter is a .m3u file flag
        isM3u = params[0].equals("m3u");

        try {
            // init url with the second given parameter
            URL url = new URL(params[1]);
            // init url connection
            urlConnection = (HttpURLConnection) url.openConnection();
            // check response code
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // init input stream
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                // read the input stream to get the needed url to play the stream
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        // .m3u files
                        if (isM3u) {
                            if (line.startsWith("#")) {
                                Log.v(UrlLoader.class.getSimpleName(), "Reader - Metadata .m3u");
                            } else if (line.length() > 0) {
                                if (line.startsWith("http://")) {
                                    result = line;
                                }
                            }
                        // .pls files
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
                // error handling
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // close input stream
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            // error handling
            } else {
                Log.e(UrlLoader.class.getSimpleName(), "Response Error!");
            }
        // error handling
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // disconnect url connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        // return streaming url
        return result;
    }
}
