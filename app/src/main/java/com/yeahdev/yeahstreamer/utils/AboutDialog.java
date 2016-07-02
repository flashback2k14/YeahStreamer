package com.yeahdev.yeahstreamer.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.yeahdev.yeahstreamer.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;


public class AboutDialog {
    /**
     * Private Member
     */
    private Context mContext;

    /**
     * Constructor
     * @param context - Android Context
     */
    public AboutDialog(Context context) {
        this.mContext = context;
    }

    /**
     * Create About Page Dialog
     */
    public void createAboutDialog() {
        // create about page
        View aboutPage = new AboutPage(this.mContext)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)
                .setDescription("Yeah! Streamer is an App to play online radio station streams!")
                .addItem(createElement("Version 0.9.9.4"))
                .addGroup("Used Libraries")
                .addItem(createElement("Android About Page by medyo", "https://github.com/medyo/android-about-page"))
                .addItem(createElement("Circular Image View by mikhaellopez", "https://github.com/lopspower/CircularImageView"))
                .addItem(createElement("Crashlytics by Twitter", "https://get.fabric.io/"))
                .addItem(createElement("Firebase Client by Firebase", "https://www.firebase.com/"))
                .addGroup("Contact with me")
                .addEmail("yeahdev@gmail.com")
                .create();
        // set about page to a dialog
        Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(aboutPage);
        dialog.show();
    }

    /**
     * Create an Element with a Title
     * @param title - Element Title
     * @return Element
     */
    private Element createElement(String title) {
        Element element = new Element();
        element.setTitle(title);
        return element;
    }

    /**
     * Create an Element with a Title and Url
     * @param title - Element Title
     * @param url - Url to Open on Item Click
     * @return Element
     */
    private Element createElement(String title, String url) {
        Element element = new Element();
        element.setTitle(title);
        element.setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)));
        return element;
    }
}
