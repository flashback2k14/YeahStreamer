package com.yeahdev.yeahstreamer.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.models.RadioStation;

import java.util.HashMap;


public class DialogWrapper {
    /**
     * Public Interfaces
     */
    public interface OnAddListener {
        void onConfirmed(RadioStation radioStation);
    }
    public interface OnEditListener {
        void onConfirmed(HashMap<String, Object> updateData);
    }
    public interface OnDeleteListener {
        void onConfirmed();
    }
    public interface OnLogoutListener {
        void onConfirmed();
    }

    /**
     * Private Member
     */
    private Context mContext;
    private Dialog mDialog;
    private ToastWrapper mToastWrapper;

    /**
     * Constructor
     * @param context - Android Context
     */
    public DialogWrapper(Context context) {
        this.mContext = context;
        this.mDialog = new Dialog(this.mContext);
        this.mDialog.setCancelable(false);
        this.mDialog.setContentView(R.layout.add_dialog);
        this.mToastWrapper = new ToastWrapper(context);
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    /**
     * Build Dialog to Add Radio Station to Firebase DB
     * @param listener - Callback Listener
     */
    public void buildAddDialog(final OnAddListener listener) {
        final EditText etName = (EditText) this.mDialog.findViewById(R.id.etRadioStationName);
        final EditText etUrl = (EditText) this.mDialog.findViewById(R.id.etRadioStationUrl);
        Button btnOk = (Button) this.mDialog.findViewById(R.id.btnOk);
        Button btnCancel = (Button) this.mDialog.findViewById(R.id.btnCancel);

        etName.setText("");
        etUrl.setText("");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    String name = etName.getText().toString();
                    String url = etUrl.getText().toString();

                    if (TextUtils.isEmpty(name)) {
                        mToastWrapper.showShort("Radio Station Name is not set!");
                        return;
                    }
                    if (TextUtils.isEmpty(url)) {
                        mToastWrapper.showShort("Radio Station URL is not set!");
                        return;
                    }

                    RadioStation radioStation = Util.getRadioStation(DialogWrapper.this.mContext, name, url);

                    etName.setText("");
                    etUrl.setText("");

                    listener.onConfirmed(radioStation);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        this.mDialog.show();
    }

    /**
     * Build Dialog to Edit Radio Station on Firebase DB
     * @param name - Radio Station Name
     * @param url - Radio Station URL
     * @param listener - Callback Listener
     */
    public void buildEditDialog(String name, String url, final OnEditListener listener) {
        final EditText etName = (EditText) this.mDialog.findViewById(R.id.etRadioStationName);
        final EditText etUrl = (EditText) this.mDialog.findViewById(R.id.etRadioStationUrl);
        Button btnOk = (Button) this.mDialog.findViewById(R.id.btnOk);
        Button btnCancel = (Button) this.mDialog.findViewById(R.id.btnCancel);

        etName.setText(name);
        etUrl.setText(url);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    String name = etName.getText().toString();
                    String url = etUrl.getText().toString();

                    if (TextUtils.isEmpty(name)) {
                        mToastWrapper.showShort("Radio Station Name is not set!");
                        return;
                    }
                    if (TextUtils.isEmpty(url)) {
                        mToastWrapper.showShort("Radio Station URL is not set!");
                        return;
                    }

                    HashMap<String, Object> updateData = new HashMap<>(2);
                    updateData.put(Constants.FIREBASE_UPDATE_NAME, name);
                    updateData.put(Constants.FIREBASE_UPDATE_URL, url);

                    etName.setText("");
                    etUrl.setText("");

                    listener.onConfirmed(updateData);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        this.mDialog.show();
    }

    /**
     * Build Dialog to Delete Radio Station on Firebase DB
     * @param listener - Callback Listener
     */
    public void buildDeleteDialog(final OnDeleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("DELETE");
        builder.setMessage("Are you sure to remove the Radio Station?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirmed();
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
    }

    /**
     * Build Dialog to logout the User
     * @param listener - Callback Listener
     */
    public void buildLogoutDialog(final OnLogoutListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setTitle("LOGOUT");
        builder.setMessage("Are you sure to logout?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onConfirmed();
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
    }
}
