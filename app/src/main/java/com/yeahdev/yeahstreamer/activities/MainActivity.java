package com.yeahdev.yeahstreamer.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.FirebaseError;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.adapter.RadioStationAdapter;
import com.yeahdev.yeahstreamer.models.RadioStation;
import com.yeahdev.yeahstreamer.service.StreamService;
import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.FirebaseWrapper;
import com.yeahdev.yeahstreamer.utils.Util;

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
    private RadioStation mCurrentRadioStation;
    private boolean mIsPlaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFbWrapper = new FirebaseWrapper(this, Constants.FIREBASE_REF);

        initControls();
        initAdapter();

        loadRadioStations();

        setupListener();
        setupBroadcastReceiver();
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
        mFbWrapper.loadData(Constants.FIREBASE_RADIOSTATION, new FirebaseWrapper.OnLoadListener() {
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
                Toast.makeText(MainActivity.this, "The read failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExpired(String msg) {
                mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                MainActivity.this.finish();
            }
        });
    }

    private void setupListener() {
        mStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        mStationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("DELETE");
                builder.setMessage("Are you sure to remove the Radio Station?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RadioStation radioStation = mStationList.get(position);

                        mFbWrapper.removeItemByKey(Constants.FIREBASE_RADIOSTATION,
                                radioStation.getKey(),
                                new FirebaseWrapper.OnChangedListener() {

                            @Override
                            public void onSuccess(String msg) {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailed(FirebaseError error) {
                                Toast.makeText(MainActivity.this, "Data could not be removed. " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onExpired(String msg) {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                MainActivity.this.finish();
                            }
                        });
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface di, int which) {

                        final Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.add_dialog);

                        final EditText etName = (EditText) dialog.findViewById(R.id.etRadioStationName);
                        final EditText etUrl = (EditText) dialog.findViewById(R.id.etRadioStationUrl);
                        Button btnOk = (Button) dialog.findViewById(R.id.btnOk);
                        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

                        final RadioStation radioStation = mStationList.get(position);
                        etName.setText(radioStation.getName());
                        etUrl.setText(radioStation.getUrl());

                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HashMap<String, Object> updateMap = new HashMap<>(2);
                                updateMap.put("name", etName.getText().toString());
                                updateMap.put("url", etUrl.getText().toString());

                                mFbWrapper.updateItemByKey(Constants.FIREBASE_RADIOSTATION,
                                        radioStation.getKey(), updateMap,
                                        new FirebaseWrapper.OnChangedListener() {

                                    @Override
                                    public void onSuccess(String msg) {
                                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onFailed(FirebaseError error) {
                                        Toast.makeText(MainActivity.this, "Data could not be updated. " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onExpired(String msg) {
                                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                        MainActivity.this.finish();
                                    }
                                });
                            }
                        });

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
            }
        });

        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsPlaying) {
                    Intent intent = new Intent(MainActivity.this, StreamService.class);
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

        mPlayerSelectedIcon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(MainActivity.this, StreamService.class);
                intent.setAction(Constants.ACTION_STOP);
                startService(intent);
                return true;
            }
        });

        mFabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.add_dialog);

                final EditText etName = (EditText) dialog.findViewById(R.id.etRadioStationName);
                final EditText etUrl = (EditText) dialog.findViewById(R.id.etRadioStationUrl);
                Button btnOk = (Button) dialog.findViewById(R.id.btnOk);
                Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String name = etName.getText().toString();
                        String url = etUrl.getText().toString();
                        RadioStation radioStation = Util.getRadioStation(MainActivity.this, name, url);

                        mFbWrapper.addItem(Constants.FIREBASE_RADIOSTATION,
                                radioStation, new FirebaseWrapper.OnChangedListener() {

                            @Override
                            public void onSuccess(String msg) {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailed(FirebaseError error) {
                                Toast.makeText(MainActivity.this, "Data could not be saved. " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onExpired(String msg) {
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                MainActivity.this.finish();
                            }
                        });
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    private void setupBroadcastReceiver() {
        BroadcastReceiver playbackStartedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mIsPlaying = true;
                byte[] imageData = Base64.decode(mCurrentRadioStation.getIcon(), Base64.DEFAULT);
                mPlayerSelectedIcon.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
                mPlayerSelectedName.setText(mCurrentRadioStation.getName());
                mPlayerControl.setImageResource(R.drawable.ic_pause_24dp);
                mTbPlayer.setVisibility(View.VISIBLE);
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

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.clear();
                editor.apply();
            }
        };
        IntentFilter playbackStoppedFilter = new IntentFilter(Constants.ACTION_PLAYBACK_STOPPED);
        LocalBroadcastManager.getInstance(this).registerReceiver(playbackStoppedReceiver, playbackStoppedFilter);

        BroadcastReceiver progressReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(Constants.EXTRA_BUFFER_PROGRESS)) {
                    int progress = intent.getIntExtra(Constants.EXTRA_BUFFER_PROGRESS, -1);
                    Toast.makeText(MainActivity.this, "Load Progress: " + progress, Toast.LENGTH_SHORT).show();
                }
            }
        };
        IntentFilter progressFilter = new IntentFilter(Constants.ACTION_PLAYBACK_PROGRESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(progressReceiver, progressFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsPlaying = preferences.getBoolean(Constants.CURRENT_PLAYING_STATE, false);

        if (preferences.contains(Constants.CURRENT_RADIO_STATION_ICON)) {
            RadioStation tmp = new RadioStation();

            tmp.setIcon(preferences.getString(Constants.CURRENT_RADIO_STATION_ICON, ""));
            tmp.setName(preferences.getString(Constants.CURRENT_RADIO_STATION_NAME, ""));
            tmp.setUrl(preferences.getString(Constants.CURRENT_RADIO_STATION_URL, ""));
            tmp.setKey(preferences.getString(Constants.CURRENT_RADIO_STATION_KEY, ""));

            mCurrentRadioStation = tmp;
        }

        if (mIsPlaying) {
            byte[] imageData = Base64.decode(mCurrentRadioStation.getIcon(), Base64.DEFAULT);
            mPlayerSelectedIcon.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
            mPlayerSelectedName.setText(mCurrentRadioStation.getName());
            mPlayerControl.setImageResource(R.drawable.ic_pause_24dp);
            mTbPlayer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(Constants.CURRENT_PLAYING_STATE, mIsPlaying);

        if (mCurrentRadioStation != null) {
            editor.putString(Constants.CURRENT_RADIO_STATION_ICON, mCurrentRadioStation.getIcon());
            editor.putString(Constants.CURRENT_RADIO_STATION_NAME, mCurrentRadioStation.getName());
            editor.putString(Constants.CURRENT_RADIO_STATION_URL, mCurrentRadioStation.getUrl());
            editor.putString(Constants.CURRENT_RADIO_STATION_KEY, mCurrentRadioStation.getKey());
        }
        editor.apply();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("LOGOUT");
                builder.setMessage("Are you sure to logout?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mFbWrapper.logout()) {
                            startActivity(new Intent(MainActivity.this, SignInActivity.class));
                            MainActivity.this.finish();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
