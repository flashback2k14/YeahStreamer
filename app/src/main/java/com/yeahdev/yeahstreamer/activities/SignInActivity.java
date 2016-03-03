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
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.FirebaseError;
import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.utils.Constants;
import com.yeahdev.yeahstreamer.utils.FirebaseWrapper;


public class SignInActivity extends AppCompatActivity {

    private EditText etEmailAddress;
    private EditText etPassword;
    private EditText etPasswordAgain;
    private Button btnSignIn;
    private Button btnRegister;
    private TextView tvRegister;

    private FirebaseWrapper mFbWrapper;
    private boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mFbWrapper = new FirebaseWrapper(Constants.FIREBASE_REF);

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
                    Toast.makeText(SignInActivity.this, "Email Address is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String password = etPassword.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignInActivity.this, "Password is empty!", Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(SignInActivity.this, "User " + authData.getUid() + " is logged in!", Toast.LENGTH_SHORT).show();
                        SignInActivity.this.finish();
                    }

                    @Override
                    public void onFailed(FirebaseError error) {
                        Toast.makeText(SignInActivity.this, "Error Code:" + error.getCode() + ", Msg: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SignInActivity.this, "Error Code:" + error.getCode() + ", Msg: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onExpired(String msg) {
                        Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
