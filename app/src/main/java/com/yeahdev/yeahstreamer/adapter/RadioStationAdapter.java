package com.yeahdev.yeahstreamer.adapter;


import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.model.RadioStation;

import java.util.ArrayList;
import java.util.Arrays;

public class RadioStationAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<RadioStation> mRadioStations;

    public RadioStationAdapter(Context context, ArrayList<RadioStation> radioStations) {
        this.mContext = context;
        this.mRadioStations = radioStations;
    }

    @Override
    public int getCount() {
        return mRadioStations.size();
    }

    @Override
    public RadioStation getItem(int position) {
        return mRadioStations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RadioStation radioStation = getItem(position);
        RadioStationViewHolder radioStationViewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.radio_station_row, parent, false);
            radioStationViewHolder = new RadioStationViewHolder();
            radioStationViewHolder.rsImageView = (ImageView) convertView.findViewById(R.id.rsImage);
            radioStationViewHolder.rsTextView = (TextView) convertView.findViewById(R.id.rsName);
            convertView.setTag(radioStationViewHolder);
        } else {
            radioStationViewHolder = (RadioStationViewHolder) convertView.getTag();
        }

        byte[] imageData = Base64.decode(radioStation.getIcon(), Base64.DEFAULT);
        radioStationViewHolder.rsImageView.setImageBitmap(BitmapFactory.decodeByteArray(imageData , 0, imageData.length));
        radioStationViewHolder.rsTextView.setText(radioStation.getName());

        return convertView;
    }

    static class RadioStationViewHolder {
        ImageView rsImageView;
        TextView rsTextView;
    }
}
