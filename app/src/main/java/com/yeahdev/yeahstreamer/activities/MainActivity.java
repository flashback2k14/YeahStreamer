package com.yeahdev.yeahstreamer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.adapter.RadioStationAdapter;
import com.yeahdev.yeahstreamer.model.RadioStation;
import com.yeahdev.yeahstreamer.services.StreamService;
import com.yeahdev.yeahstreamer.util.Constants;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ArrayList<RadioStation> mStationList;
    private RadioStationAdapter mStationAdapter;

    private ListView mStationListview;

    private Toolbar mTbPlayer;
    private ImageView mPlayerSelectedIcon;
    private TextView mPlayerSelectedName;
    private ImageView mPlayerControl;

    private RadioStation mCurrentRadioStation;
    private boolean mIsPlaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControls();
        initAdapter();

        loadRadioStations();

        setupListener();
        setupBroadcastReceiver();
    }

    private void initControls() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStationListview = (ListView) findViewById(R.id.lvRadioStations);

        mTbPlayer = (Toolbar) findViewById(R.id.tbPlayer);
        mPlayerSelectedIcon = (ImageView) findViewById(R.id.selected_track_image);
        mPlayerSelectedName = (TextView) findViewById(R.id.selected_track_title);
        mPlayerControl = (ImageView) findViewById(R.id.player_control);

        mTbPlayer.setVisibility(View.GONE);
    }

    private void initAdapter() {
        mStationList = new ArrayList<>();
        mStationAdapter = new RadioStationAdapter(this, mStationList);
        mStationListview.setAdapter(mStationAdapter);
    }

    private void setupListener() {
        mStationListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RadioStation radioStation = mStationList.get(position);

                mCurrentRadioStation = radioStation;
                mIsPlaying = true;

                Intent intent = new Intent(MainActivity.this, StreamService.class);
                intent.putExtra(Constants.EXTRA_STATION_NAME, radioStation.getName());
                intent.putExtra(Constants.EXTRA_STATION_URI, radioStation.getUrl());
                intent.setAction(Constants.ACTION_PLAY);

                startService(intent);
            }
        });

        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsPlaying) {
                    Intent intent = new Intent(MainActivity.this, StreamService.class);
                    intent.putExtra(Constants.EXTRA_STATION_NAME, mCurrentRadioStation.getName());
                    intent.putExtra(Constants.EXTRA_STATION_URI, mCurrentRadioStation.getUrl());
                    intent.setAction(Constants.ACTION_PAUSE);
                    startService(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, StreamService.class);
                    intent.putExtra(Constants.EXTRA_STATION_NAME, mCurrentRadioStation.getName());
                    intent.putExtra(Constants.EXTRA_STATION_URI, mCurrentRadioStation.getUrl());
                    intent.setAction(Constants.ACTION_PLAY);
                    startService(intent);
                }
            }
        });
    }

    private void setupBroadcastReceiver() {
        BroadcastReceiver playbackStartedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mIsPlaying = true;
                mTbPlayer.setVisibility(View.VISIBLE);
                mPlayerSelectedIcon.setImageBitmap(BitmapFactory.decodeByteArray(mCurrentRadioStation.getIcon(), 0, mCurrentRadioStation.getIcon().length));
                mPlayerSelectedName.setText(mCurrentRadioStation.getName());
                mPlayerControl.setImageResource(R.drawable.ic_pause_24dp);
            }
        };
        IntentFilter playbackStartedFilter = new IntentFilter(Constants.ACTION_PLAYBACK_STARTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackStartedReceiver, playbackStartedFilter);

        BroadcastReceiver playbackPausedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mIsPlaying = false;
                mPlayerControl.setImageResource(R.drawable.ic_play_arrow_24dp);
            }
        };
        IntentFilter playbackPausedFilter = new IntentFilter(Constants.ACTION_PLAYBACK_PAUSED);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackPausedReceiver, playbackPausedFilter);

        BroadcastReceiver playbackStoppedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mIsPlaying = false;
                mCurrentRadioStation = null;
                mPlayerSelectedIcon.setImageBitmap(null);
                mPlayerSelectedName.setText("");
                mPlayerControl.setImageResource(R.drawable.ic_stop_24dp);
                mTbPlayer.setVisibility(View.GONE);
            }
        };
        IntentFilter playbackStoppedFilter = new IntentFilter(Constants.ACTION_PLAYBACK_STOPPED);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackStoppedReceiver, playbackStoppedFilter);
    }

    private void loadRadioStations() {
        RadioStation radioStation = new RadioStation();
        radioStation.setIcon(createPlaceholderIcon());
        radioStation.setName("Radio Fritz");
        radioStation.setUrl("http://fritz.de/livemp3");

        RadioStation radioStation1 = new RadioStation();
        radioStation1.setIcon(createPlaceholderIcon());
        radioStation1.setName("MDR Info");
        radioStation1.setUrl("http://c22033-ls.i.core.cdn.streamfarm.net/QpZptC4ta9922033/22033mdr/live/app2128740352/w2128904192/live_de_56.mp3");

        mStationList.clear();
        mStationList.add(radioStation);
        mStationList.add(radioStation1);
        mStationAdapter.notifyDataSetChanged();
    }

    private byte[] createPlaceholderIcon() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
