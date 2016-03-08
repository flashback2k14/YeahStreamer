package com.yeahdev.yeahstreamer.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.FirebaseError;
import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.adapter.RadioStationRvAdapter;
import com.yeahdev.yeahstreamer.interfaces.IItemButtonClicked;
import com.yeahdev.yeahstreamer.models.RadioStation;
import com.yeahdev.yeahstreamer.service.StreamService;
import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.DialogWrapper;
import com.yeahdev.yeahstreamer.utils.FirebaseWrapper;
import com.yeahdev.yeahstreamer.utils.PreferenceWrapper;
import com.yeahdev.yeahstreamer.utils.ToastWrapper;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements IItemButtonClicked {

    private ArrayList<RadioStation> mStationList;
    private RadioStationRvAdapter mStationRvAdapter;
    private FloatingActionButton mFabAdd;

    private ProgressDialog mProgressDialog;
    private LinearLayout mLlNoStationsAvailable;
    private RecyclerView mStationRecyclerView;

    private Toolbar mTbPlayer;
    private ImageView mPlayerSelectedIcon;
    private TextView mPlayerSelectedName;
    private ImageView mPlayerControl;
    private ImageView mPlayerControlStop;

    private FirebaseWrapper mFbWrapper;
    private DialogWrapper mDialogWrapper;
    private ToastWrapper mToastWrapper;
    private PreferenceWrapper mPreferenceWrapper;

    private RadioStation mCurrentRadioStation;
    private boolean mIsPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWrapper();
        initControls();
        initAdapter();

        loadRadioStations();

        setupListener();
        setupBroadcastReceiver();
    }

    private void initWrapper() {
        mFbWrapper = new FirebaseWrapper(Constants.FIREBASE_REF);
        mDialogWrapper = new DialogWrapper(this);
        mToastWrapper = new ToastWrapper(this);
        mPreferenceWrapper = new PreferenceWrapper(PreferenceManager.getDefaultSharedPreferences(this));
    }

    private void initControls() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressDialog = ProgressDialog.show(this, "Loading", "Get Radio Stations from Firebase...", false, true);
        mFabAdd = (FloatingActionButton) findViewById(R.id.fab);

        mLlNoStationsAvailable = (LinearLayout) findViewById(R.id.llNoStationsAvailable);

        mStationRecyclerView = (RecyclerView) findViewById(R.id.rvRadioStations);
        mStationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mStationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mStationRecyclerView.setHasFixedSize(true);

        mTbPlayer = (Toolbar) findViewById(R.id.tbPlayer);
        mTbPlayer.setVisibility(View.GONE);

        mPlayerSelectedIcon = (ImageView) findViewById(R.id.current_radio_station_logo);
        mPlayerSelectedName = (TextView) findViewById(R.id.current_radio_station_name);
        mPlayerControl = (ImageView) findViewById(R.id.player_control);
        mPlayerControlStop = (ImageView) findViewById(R.id.player_control_stop);
    }

    private void initAdapter() {
        mStationList = new ArrayList<>();
        mStationRvAdapter = new RadioStationRvAdapter(this.mStationList, this);
        mStationRecyclerView.setAdapter(mStationRvAdapter);
    }

    private void loadRadioStations() {
        mFbWrapper.loadData(Constants.FIREBASE_ROUTE_RADIOSTATION, new FirebaseWrapper.OnLoadListener() {
            @Override
            public void onLoaded(ArrayList<RadioStation> radioStations) {
                if (radioStations.size() == 0) {
                    mLlNoStationsAvailable.setVisibility(View.VISIBLE);
                    mProgressDialog.dismiss();
                } else {
                    mLlNoStationsAvailable.setVisibility(View.GONE);
                    mStationList.clear();
                    mStationList.addAll(radioStations);
                    mStationRvAdapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onCanceled(FirebaseError error) {
                mProgressDialog.dismiss();
                mToastWrapper.showLong("The read failed: " + error.getMessage());
                switch (error.getCode()) {
                    case FirebaseError.AUTHENTICATION_PROVIDER_DISABLED:
                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        MainActivity.this.finish();
                        break;
                    case FirebaseError.PERMISSION_DENIED:
                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        MainActivity.this.finish();
                        break;
                    case FirebaseError.PROVIDER_ERROR:
                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                        MainActivity.this.finish();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onExpired(String msg) {
                mProgressDialog.dismiss();
                mToastWrapper.showShort(msg);
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                MainActivity.this.finish();
            }
        });
    }

    private void setupListener() {
        mFabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogWrapper.buildAddDialog(new DialogWrapper.OnAddListener() {
                    @Override
                    public void onConfirmed(RadioStation radioStation) {
                        mFbWrapper.addItem(Constants.FIREBASE_ROUTE_RADIOSTATION,
                                radioStation, new FirebaseWrapper.OnChangedListener() {
                                    @Override
                                    public void onSuccess(String msg) {
                                        mToastWrapper.showShort(msg);
                                        mDialogWrapper.getDialog().dismiss();
                                    }

                                    @Override
                                    public void onFailed(FirebaseError error) {
                                        mToastWrapper.showLong("Data could not be saved. " + error.getMessage());
                                    }

                                    @Override
                                    public void onExpired(String msg) {
                                        mToastWrapper.showShort(msg);
                                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                        MainActivity.this.finish();
                                    }
                                });
                    }
                });
            }
        });

        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsPlaying) {
                    // start service to pause stream
                    Intent intent = new Intent(MainActivity.this, StreamService.class);
                    intent.setAction(Constants.ACTION_PAUSE);
                    startService(intent);
                } else {
                    if (mCurrentRadioStation != null) {
                        // start service to play stream
                        Intent intent = new Intent(MainActivity.this, StreamService.class);
                        intent.putExtra(Constants.EXTRA_STATION_NAME, mCurrentRadioStation.getName());
                        intent.putExtra(Constants.EXTRA_STATION_URI, mCurrentRadioStation.getUrl());
                        intent.setAction(Constants.ACTION_PLAY);
                        startService(intent);
                    } else {
                        mToastWrapper.showLong("Something went wrong to play the Stream - mPlayerControl.");
                    }
                }
            }
        });

        mPlayerControlStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start service to stop stream
                Intent intent = new Intent(MainActivity.this, StreamService.class);
                intent.setAction(Constants.ACTION_STOP);
                startService(intent);
            }
        });
    }

    private void setupBroadcastReceiver() {
        // BroadcastReceiver for played state from StreamService
        BroadcastReceiver playbackStartedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mCurrentRadioStation != null) {
                    // set playback state
                    mIsPlaying = true;
                    // set player layout to pause state
                    byte[] imageData = Base64.decode(mCurrentRadioStation.getIcon(), Base64.DEFAULT);
                    mPlayerSelectedIcon.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                    mPlayerSelectedName.setText(mCurrentRadioStation.getName());
                    mPlayerControl.setImageResource(R.drawable.ic_pause_24dp);
                    mTbPlayer.setVisibility(View.VISIBLE);
                    // save data
                    mPreferenceWrapper.setPlaybackState(mIsPlaying);
                    mPreferenceWrapper.setCurrentRadioStation(mCurrentRadioStation);
                }
            }
        };
        IntentFilter playbackStartedFilter = new IntentFilter(Constants.ACTION_PLAYBACK_STARTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackStartedReceiver, playbackStartedFilter);
        // BroadcastReceiver for paused state from StreamService
        BroadcastReceiver playbackPausedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // change playback state
                mIsPlaying = false;
                // change player layout to play state
                mPlayerControl.setImageResource(R.drawable.ic_play_arrow_24dp);
                // save data
                mPreferenceWrapper.setPlaybackState(mIsPlaying);
            }
        };
        IntentFilter playbackPausedFilter = new IntentFilter(Constants.ACTION_PLAYBACK_PAUSED);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackPausedReceiver, playbackPausedFilter);
        // BroadcastReceiver for stopped state from StreamService
        BroadcastReceiver playbackStoppedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // reset data
                mIsPlaying = false;
                mCurrentRadioStation = null;
                // reset player layout
                mPlayerSelectedIcon.setImageBitmap(null);
                mPlayerSelectedName.setText("");
                mTbPlayer.setVisibility(View.GONE);
                // reset data
                mPreferenceWrapper.resetPlaybackState();
                mPreferenceWrapper.resetCurrentRadioStation();
            }
        };
        IntentFilter playbackStoppedFilter = new IntentFilter(Constants.ACTION_PLAYBACK_STOPPED);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackStoppedReceiver, playbackStoppedFilter);
        // BroadcastReceiver for buffer progress state from StreamService
        BroadcastReceiver progressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(Constants.EXTRA_BUFFER_PROGRESS)) {
                    int progress = intent.getIntExtra(Constants.EXTRA_BUFFER_PROGRESS, -1);
                    mToastWrapper.showShort("YEAH! Streamer - Buffer Progress: " + progress);
                }
            }
        };
        IntentFilter progressFilter = new IntentFilter(Constants.ACTION_PLAYBACK_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(progressReceiver, progressFilter);
        // BroadcastReceiver for onError or onInfo state from StreamService
        BroadcastReceiver errorInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(Constants.EXTRA_INFO_ERROR_MSG)) {
                    String errorInfo = intent.getStringExtra(Constants.EXTRA_INFO_ERROR_MSG);
                    mToastWrapper.showLong(errorInfo);
                }
            }
        };
        IntentFilter errorInfoFilter = new IntentFilter(Constants.EXTRA_INFO_ERROR_TYPE);
        LocalBroadcastManager.getInstance(this).registerReceiver(errorInfoReceiver, errorInfoFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // get data
        mIsPlaying = mPreferenceWrapper.getPlaybackState();
        mCurrentRadioStation = mPreferenceWrapper.getCurrentRadioStation();
        // set player layout state
        if (mIsPlaying) {
            if (mCurrentRadioStation != null) {
                // set player layout to pause state
                byte[] imageData = Base64.decode(mCurrentRadioStation.getIcon(), Base64.DEFAULT);
                mPlayerSelectedIcon.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                mPlayerSelectedName.setText(mCurrentRadioStation.getName());
                mPlayerControl.setImageResource(R.drawable.ic_pause_24dp);
                mTbPlayer.setVisibility(View.VISIBLE);
            }
        } else {
            if (mCurrentRadioStation != null) {
                // set player layout to play state
                byte[] imageData = Base64.decode(mCurrentRadioStation.getIcon(), Base64.DEFAULT);
                mPlayerSelectedIcon.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                mPlayerSelectedName.setText(mCurrentRadioStation.getName());
                mPlayerControl.setImageResource(R.drawable.ic_play_arrow_24dp);
                mTbPlayer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save data
        mPreferenceWrapper.setPlaybackState(mIsPlaying);
        if (mCurrentRadioStation != null) {
           mPreferenceWrapper.setCurrentRadioStation(mCurrentRadioStation);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFbWrapper.removeListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                mDialogWrapper.buildLogoutDialog(new DialogWrapper.OnLogoutListener() {
                    @Override
                    public void onConfirmed() {
                        mToastWrapper.showShort("logging out...");
                        if (mFbWrapper.logout()) {
                            mFbWrapper.removeListener();
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            MainActivity.this.finish();
                        }
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPlayRadioStation(RadioStation radioStation) {
        //set data
        mCurrentRadioStation = radioStation;
        mIsPlaying = true;
        // start service to play stream
        Intent intent = new Intent(MainActivity.this, StreamService.class);
        intent.putExtra(Constants.EXTRA_STATION_NAME, mCurrentRadioStation.getName());
        intent.putExtra(Constants.EXTRA_STATION_URI, mCurrentRadioStation.getUrl());
        intent.setAction(Constants.ACTION_PLAY);
        startService(intent);
    }

    @Override
    public void onEditRadioStation(final RadioStation radioStation) {
        mDialogWrapper.buildEditDialog(radioStation.getName(), radioStation.getUrl(), new DialogWrapper.OnEditListener() {
            @Override
            public void onConfirmed(HashMap<String, Object> updateData) {
                mFbWrapper.updateItemByKey(Constants.FIREBASE_ROUTE_RADIOSTATION,
                    radioStation.getKey(), updateData,
                    new FirebaseWrapper.OnChangedListener() {
                        @Override
                        public void onSuccess(String msg) {
                            mToastWrapper.showShort(msg);
                            mDialogWrapper.getDialog().dismiss();
                        }

                        @Override
                        public void onFailed(FirebaseError error) {
                            mToastWrapper.showLong("Data could not be updated. " + error.getMessage());
                        }

                        @Override
                        public void onExpired(String msg) {
                            mToastWrapper.showShort(msg);
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            MainActivity.this.finish();
                        }
                    });
            }
        });
    }

    @Override
    public void onDeleteRadioStation(final RadioStation radioStation) {
        mDialogWrapper.buildDeleteDialog(new DialogWrapper.OnDeleteListener() {
            @Override
            public void onConfirmed() {
                mFbWrapper.removeItemByKey(Constants.FIREBASE_ROUTE_RADIOSTATION,
                    radioStation.getKey(),
                    new FirebaseWrapper.OnChangedListener() {
                        @Override
                        public void onSuccess(String msg) {
                            mToastWrapper.showShort(msg);
                        }

                        @Override
                        public void onFailed(FirebaseError error) {
                            mToastWrapper.showLong("Data could not be removed. " + error.getMessage());
                        }

                        @Override
                        public void onExpired(String msg) {
                            mToastWrapper.showShort(msg);
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            MainActivity.this.finish();
                        }
                    });
            }
        });
    }
}
