package com.yeahdev.yeahstreamer.util;


import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class UrlLoader extends AsyncTask<String, Void, String> {
    HttpClient httpClient;
    HttpGet getRequest;
    HttpResponse httpResponse;
    String result;
    boolean isM3u;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        httpClient = new DefaultHttpClient();
        getRequest = null;
        httpResponse = null;
        result = "";
    }

    @Override
    protected String doInBackground(String... params) {

        isM3u = params[0].equals("m3u");

        try {
            getRequest = new HttpGet(params[1]);
            httpResponse = httpClient.execute(getRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (httpResponse != null) {
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.e(UrlLoader.class.getSimpleName(), "Response Error!");
            } else {
                InputStream inputStream = null;

                try {
                    inputStream = httpResponse.getEntity().getContent();
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                                } else {
                                    result = getRequest.getURI().resolve(line).toString();
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
            }
        }

        return result;
    }
}
