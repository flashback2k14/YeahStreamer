package com.yeahdev.yeahstreamer.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.FirebaseWrapper;
import com.yeahdev.yeahstreamer.utils.ToastWrapper;


public class SignInActivity extends AppCompatActivity {

    private EditText etEmailAddress;
    private EditText etPassword;
    private EditText etPasswordAgain;
    private Button btnSignIn;
    private Button btnRegister;
    private TextView tvRegister;

    private FirebaseWrapper mFbWrapper;
    private ToastWrapper mToastWrapper;
    private boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mFbWrapper = new FirebaseWrapper(Constants.FIREBASE_REF);
        mToastWrapper = new ToastWrapper(this);

        String userId = mFbWrapper.getUserId();
        if (userId != null) {
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
                    mToastWrapper.showShort("Email Address is empty!");
                    return;
                }

                String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mToastWrapper.showShort("Password is empty!");
                    return;
                }

                mFbWrapper.authWithPassword(emailAddress, password, new FirebaseWrapper.OnAuthListener() {
                    @Override
                    public void onSuccess(AuthData authData) {
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

                        mToastWrapper.showLong("User " + authData.getProviderData().get("email") + " is logged in!");
                        SignInActivity.this.finish();
                    }

                    @Override
                    public void onFailed(FirebaseError error) {
                        mToastWrapper.showLong("Error Code:" + error.getCode() + ", Msg: " + error.getMessage());
                    }
                });
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String emailAddress = etEmailAddress.getText().toString();
                if (TextUtils.isEmpty(emailAddress)) {
                    mToastWrapper.showShort("Email Address is empty!");
                    return;
                }

                final String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mToastWrapper.showShort("Password is empty!");
                    return;
                }

                String passwordAgain = etPasswordAgain.getText().toString();
                if (TextUtils.isEmpty(passwordAgain)) {
                    mToastWrapper.showShort("Password again is empty!");
                    return;
                }

                if (!password.matches(passwordAgain)) {
                    mToastWrapper.showShort("Passwords not matching!");
                    etPassword.setText("");
                    etPasswordAgain.setText("");
                    return;
                }

                mFbWrapper.createAndLoginUser(emailAddress, password, new FirebaseWrapper.OnCreatedListener() {
                    @Override
                    public void onSuccess(String msg) {
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

                    @Override
                    public void onFailed(FirebaseError error) {
                        mToastWrapper.showLong("Error Code:" + error.getCode() + ", Msg: " + error.getMessage());
                    }

                    @Override
                    public void onExpired(String msg) {
                        mToastWrapper.showShort(msg);
                    }
                });
            }
        });
    }
}
