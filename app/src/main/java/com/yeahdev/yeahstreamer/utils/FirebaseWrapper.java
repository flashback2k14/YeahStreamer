package com.yeahdev.yeahstreamer.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yeahdev.yeahstreamer.models.RadioStation;
import com.yeahdev.yeahstreamer.models.User;

import java.util.ArrayList;
import java.util.HashMap;


public class FirebaseWrapper {
    /**
     * Interfaces
     */
    public interface OnCreatedListener {
        void onSuccess(String msg);
        void onFailed(FirebaseError error);
        void onExpired(String msg);
    }
    public interface OnAuthListener {
        void onSuccess(AuthData authData);
        void onFailed(FirebaseError error);
    }
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

    /**
     * Members
     */
    private Context mContext;
    private String mBaseUrl;
    private Firebase mBaseRef;

    /**
     * Constructor
     * @param context
     * @param baseUrl
     */
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
    public String getUserId() {
        AuthData authData = this.mBaseRef.getAuth();
        if (authData != null) {
            return authData.getUid();
        } else {
            return null;
        }
    }

    /**
     * Logout the User
     * @return User is logged out
     */
    public boolean logout() {
        this.mBaseRef.unauth();
        return true;
    }

    /**
     * BEGIN AUTH
     */
    /**
     * Authentificate the User with Email and Password
     * @param email
     * @param password
     * @param listener
     */
    public void authWithPassword(String email, String password, final OnAuthListener listener) {
        this.mBaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                if (listener != null) {
                    listener.onSuccess(authData);
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                if (listener != null) {
                    listener.onFailed(firebaseError);
                }
            }
        });
    }

    /**
     * Create a new User, Save User Data on Firebase DB and login the User
     * @param email
     * @param password
     * @param listener
     */
    public void createAndLoginUser(final String email, final String password, final OnCreatedListener listener) {
        this.mBaseRef.createUser(email, password, new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                authWithPassword(email, password, new OnAuthListener() {
                    @Override
                    public void onSuccess(AuthData authData) {
                        if (listener != null) {

                            User user = new User();
                            user.setUsername(Util.getUsername(authData.getProviderData().get("email")));
                            user.setEmailAddress(authData.getProviderData().get("email").toString());
                            user.setProfileImage(authData.getProviderData().get("profileImageURL").toString());
                            user.setProvider(authData.getProvider());

                            addItem(Constants.FIREBASE_USER, user, new OnChangedListener() {
                                @Override
                                public void onSuccess(String msg) {
                                    listener.onSuccess(msg);
                                }

                                @Override
                                public void onFailed(FirebaseError error) {
                                    listener.onFailed(error);
                                }

                                @Override
                                public void onExpired(String msg) {
                                    listener.onExpired(msg);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailed(FirebaseError error) {
                        if (listener != null) {
                            listener.onFailed(error);
                        }
                    }
                });
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                if (listener != null) {
                    listener.onFailed(firebaseError);
                }
            }
        });
    }
    /**
     * END AUTH
     */

    /**
     * BEGIN CRUD
     */
    /**
     * Add new Item to specific Route on Firebase DB
     * @param route
     * @param item Radio Station
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
     * Add new Item to specific Route on Firebase DB
     * @param route
     * @param user
     * @param listener
     */
    public void addItem(String route, User user, final OnChangedListener listener) {
        String userId = getUserId();
        if (userId != null) {
            Firebase userRef = this.mBaseRef.child(route).child(userId);
            userRef.setValue(user, new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError != null) {
                        if (listener != null) {
                            listener.onFailed(firebaseError);
                        }
                    } else {
                        if (listener != null) {
                            listener.onSuccess("User successfully saved!");
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