package com.yeahdev.yeahstreamer.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yeahdev.yeahstreamer.models.RadioStation;

import java.util.ArrayList;
import java.util.HashMap;


public class FirebaseWrapper {

    public interface OnLoadListener {
        void onLoaded(ArrayList<RadioStation> radioStations);
        void onCanceled(FirebaseError error);
        void onExpired(String msg);
    }

    public interface OnChangedListener {
        void onSuccess(String msg);
        void onFailed(FirebaseError error);
        void onExpired(String msg);
    }

    private Context mContext;
    private String mBaseUrl;
    private Firebase mBaseRef;

    public FirebaseWrapper(Context context, String baseUrl) {
        this.mContext = context;
        this.mBaseUrl = baseUrl;
        this.mBaseRef = new Firebase(this.mBaseUrl);
    }

    /**
     * Check if User is logged in and Return the specific User Id
     * @return UserId
     */
    @Nullable
    private String getUserId() {
        AuthData authData = this.mBaseRef.getAuth();
        if (authData != null) {
            return authData.getUid();
        } else {
            return null;
        }
    }

    /**
     * BEGIN CRUD
     */
    /**
     * Add new Item to specific Route on Firebase DB
     * @param route
     * @param item
     * @param listener
     */
    public void addItem(String route, RadioStation item, final OnChangedListener listener) {
        String userId = getUserId();
        if (userId != null) {
            Firebase radioStationRef = this.mBaseRef.child(route).child(userId).push();
            item.setKey(radioStationRef.getKey());
            radioStationRef.setValue(item, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        if (listener != null) {
                            listener.onFailed(firebaseError);
                        }
                    } else {
                        if (listener != null) {
                            listener.onSuccess("Radio Station successfully saved!");
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onExpired("User login expired!");
            }
        }
    }

    /**
     * Load all data from specific Route on Firebase DB
     * @param route
     * @param listener
     */
    public void loadData(String route, final OnLoadListener listener) {
        String userId = getUserId();
        if (userId != null) {
            Firebase userStationRef = this.mBaseRef.child(route).child(userId);
            userStationRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (listener != null) {
                        ArrayList<RadioStation> tmpRadioStations = new ArrayList<>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            tmpRadioStations.add(child.getValue(RadioStation.class));
                        }
                        listener.onLoaded(tmpRadioStations);
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (listener != null) {
                        listener.onCanceled(firebaseError);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onExpired("User login expired!");
            }
        }
    }

    /**
     * Update specific Firebase Item on specific Firebase DB Route by KEY
     * @param route
     * @param itemKey
     * @param updateData
     * @param listener
     */
    public void updateItemByKey(String route, String itemKey, HashMap<String, Object> updateData, final OnChangedListener listener) {
        String userId = getUserId();
        if (userId != null) {
            Firebase radioStationRef = this.mBaseRef.child(route).child(userId).child(itemKey);
            radioStationRef.updateChildren(updateData, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        if (listener != null) {
                            listener.onFailed(firebaseError);
                        }
                    } else {
                        if (listener != null) {
                            listener.onSuccess("Radio Station successfully updated!");
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onExpired("User login expired!");
            }
        }
    }

    /**
     * Remove specific Firebase Item on specific Firebase DB Route by KEY
     * @param route
     * @param itemKey
     * @param listener
     */
    public void removeItemByKey(String route, String itemKey, final OnChangedListener listener) {
        String userId = getUserId();
        if (userId != null) {
            Firebase radioStationRef = this.mBaseRef.child(route).child(userId).child(itemKey);
            radioStationRef.removeValue(new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        if (listener != null) {
                            listener.onFailed(firebaseError);
                        }
                    } else {
                        if (listener != null) {
                            listener.onSuccess("Radio Station successfully removed!");
                        }
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onExpired("User login expired!");
            }
        }
    }
    /**
     * END CRUD
     */
}
