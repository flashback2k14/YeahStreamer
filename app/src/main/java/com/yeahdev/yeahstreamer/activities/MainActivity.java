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
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.FirebaseError;
import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.adapter.RadioStationAdapter;
import com.yeahdev.yeahstreamer.models.RadioStation;
import com.yeahdev.yeahstreamer.service.StreamService;
import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.DialogWrapper;
import com.yeahdev.yeahstreamer.utils.FirebaseWrapper;
import com.yeahdev.yeahstreamer.utils.PreferenceWrapper;
import com.yeahdev.yeahstreamer.utils.ToastWrapper;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private ArrayList<RadioStation> mStationList;
    private RadioStationAdapter mStationAdapter;
    private FloatingActionButton mFabAdd;

    private ProgressDialog mProgressDialog;
    private ListView mStationListView;

    private Toolbar mTbPlayer;
    private ImageView mPlayerSelectedIcon;
    private TextView mPlayerSelectedName;
    private ImageView mPlayerControl;

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

        mFabAdd = (FloatingActionButton) findViewById(R.id.fab);

        mProgressDialog = ProgressDialog.show(this, "Loading", "Get Radio Stations from Firebase...", false, true);
        mStationListView = (ListView) findViewById(R.id.lvRadioStations);

        mTbPlayer = (Toolbar) findViewById(R.id.tbPlayer);
        mPlayerSelectedIcon = (ImageView) findViewById(R.id.selected_track_image);
        mPlayerSelectedName = (TextView) findViewById(R.id.selected_track_title);
        mPlayerControl = (ImageView) findViewById(R.id.player_control);

        mTbPlayer.setVisibility(View.GONE);
    }

    private void initAdapter() {
        mStationList = new ArrayList<>();
        mStationAdapter = new RadioStationAdapter(this, mStationList);
        mStationListView.setAdapter(mStationAdapter);
    }

    private void loadRadioStations() {
        mFbWrapper.loadData(Constants.FIREBASE_ROUTE_RADIOSTATION, new FirebaseWrapper.OnLoadListener() {
            @Override
            public void onLoaded(ArrayList<RadioStation> radioStations) {
                mStationList.clear();
                mStationList.addAll(radioStations);
                mStationAdapter.notifyDataSetChanged();
                mProgressDialog.dismiss();
            }

            @Override
            public void onCanceled(FirebaseError error) {
                mProgressDialog.dismiss();
                mToastWrapper.showLong("The read failed: " + error.getMessage());
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
        mStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // set data
                mCurrentRadioStation = mStationList.get(position);
                mIsPlaying = true;
                // start service to play stream
                Intent intent = new Intent(MainActivity.this, StreamService.class);
                intent.putExtra(Constants.EXTRA_STATION_NAME, mCurrentRadioStation.getName());
                intent.putExtra(Constants.EXTRA_STATION_URI, mCurrentRadioStation.getUrl());
                intent.setAction(Constants.ACTION_PLAY);
                startService(intent);
            }
        });

        mStationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                mDialogWrapper.buildDeleteDialog(new DialogWrapper.OnDeleteListener() {
                    @Override
                    public void onConfirmed() {
                        RadioStation radioStation = mStationList.get(position);
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

                    @Override
                    public void onEdited() {
                        final RadioStation radioStation = mStationList.get(position);
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
                });
                return true;
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

        mPlayerSelectedIcon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // start service to stop stream
                Intent intent = new Intent(MainActivity.this, StreamService.class);
                intent.setAction(Constants.ACTION_STOP);
                startService(intent);
                return true;
            }
        });

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
    }

    private void setupBroadcastReceiver() {
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

        BroadcastReceiver progressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(Constants.EXTRA_BUFFER_PROGRESS)) {
                    int progress = intent.getIntExtra(Constants.EXTRA_BUFFER_PROGRESS, -1);
                    mToastWrapper.showShort("Load Progress: " + progress);
                }
            }
        };
        IntentFilter progressFilter = new IntentFilter(Constants.ACTION_PLAYBACK_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(progressReceiver, progressFilter);
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
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            MainActivity.this.finish();
                        }
                    }
                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
