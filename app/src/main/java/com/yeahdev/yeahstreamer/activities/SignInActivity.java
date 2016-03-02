package com.yeahdev.yeahstreamer.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.models.User;
import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.Util;


public class SignInActivity extends AppCompatActivity {

    private EditText etEmailAddress;
    private EditText etPassword;
    private EditText etPasswordAgain;
    private Button btnSignIn;
    private Button btnRegister;
    private TextView tvRegister;

    private boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        AuthData mAuth = new Firebase(Constants.FIREBASE_REF).getAuth();
        if (mAuth != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            SignInActivity.this.finish();
        } else {
            initComponents();
            setupListener();
        }
    }

    private void initComponents() {
        etEmailAddress = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPasswordAgain = (EditText) findViewById(R.id.etPasswordAgain);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        tvRegister = (TextView) findViewById(R.id.tvRegister);
    }

    private void setupListener() {
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin) {
                    etPasswordAgain.setVisibility(View.VISIBLE);
                    btnRegister.setVisibility(View.VISIBLE);
                    btnSignIn.setVisibility(View.GONE);
                    tvRegister.setText(getResources().getString(R.string.gtLogin));
                    isLogin = false;
                } else {
                    etPasswordAgain.setVisibility(View.GONE);
                    btnRegister.setVisibility(View.GONE);
                    btnSignIn.setVisibility(View.VISIBLE);
                    tvRegister.setText(getResources().getString(R.string.register));
                    isLogin = true;
                }
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailAddress = etEmailAddress.getText().toString();
                if (TextUtils.isEmpty(emailAddress)) {
                    Toast.makeText(SignInActivity.this, "Email Address is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignInActivity.this, "Password is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Firebase mRef = new Firebase(Constants.FIREBASE_REF);
                mRef.authWithPassword(emailAddress, password, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        etEmailAddress.setText("");
                        etPassword.setText("");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            View view = btnSignIn;
                            String transitionName = getResources().getString(R.string.signinToMain);
                            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SignInActivity.this, view, transitionName);
                            startActivity(intent, activityOptions.toBundle());
                        } else {
                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                        }

                        SignInActivity.this.finish();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        Toast.makeText(SignInActivity.this, "Error Code:" + firebaseError.getCode() + ", Msg: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String emailAddress = etEmailAddress.getText().toString();
                if (TextUtils.isEmpty(emailAddress)) {
                    Toast.makeText(SignInActivity.this, "Email Address is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignInActivity.this, "Password is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String passwordAgain = etPasswordAgain.getText().toString();
                if (TextUtils.isEmpty(passwordAgain)) {
                    Toast.makeText(SignInActivity.this, "Password again is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.matches(passwordAgain)) {
                    Toast.makeText(SignInActivity.this, "Passwords not matching!", Toast.LENGTH_SHORT).show();
                    etPassword.setText("");
                    etPasswordAgain.setText("");
                    return;
                }

                final Firebase mRef = new Firebase(Constants.FIREBASE_REF);
                mRef.createUser(emailAddress, password, new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {

                        mRef.authWithPassword(emailAddress, password, new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(final AuthData authData) {

                                User user = new User();
                                user.setUsername(Util.getUsername(authData.getProviderData().get("email")));
                                user.setEmailAddress(authData.getProviderData().get("email").toString());
                                user.setProfileImage(authData.getProviderData().get("profileImageURL").toString());
                                user.setProvider(authData.getProvider());

                                Firebase userRef = mRef.child("users").child(authData.getUid());
                                userRef.setValue(user, new Firebase.CompletionListener() {
                                    @Override
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                        if (firebaseError != null) {
                                           Toast.makeText(SignInActivity.this, "Data could not be saved. " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            etEmailAddress.setText("");
                                            etPassword.setText("");
                                            etPasswordAgain.setText("");

                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                                View view = btnSignIn;
                                                String transitionName = getResources().getString(R.string.signinToMain);
                                                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SignInActivity.this, view, transitionName);
                                                startActivity(intent, activityOptions.toBundle());
                                            } else {
                                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }

                                            SignInActivity.this.finish();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                Toast.makeText(SignInActivity.this, "Error Code:" + firebaseError.getCode() + ", Msg: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Toast.makeText(SignInActivity.this, "Error Code:" + firebaseError.getCode() + ", Msg: " + firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
