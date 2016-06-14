package com.example.tushar.twitterproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Created by tushar on 12/6/16.
 */
public class Preferences {
    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String ACCESS_TOKEN_SECRET = "ACCESS_TOKEN_SECRET";
    private static final String NAME = "NAME";
    private static final String URL = "IMAGE_URL";
    private static Preferences mInstance;
    private Context mContext;
    //
    private SharedPreferences mPrefs;

    private Preferences(Context applicationContext) {
        mContext = applicationContext;
        Initialize(mContext);
    }

    public static Preferences getInstance(Context ctxt) {
        if (mInstance == null) {
            mInstance = new Preferences(ctxt.getApplicationContext());
        }

        return mInstance;
    }

    public void Initialize(Context ctxt) {
        mContext = ctxt;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String getAccessToken() {
        return mPrefs.getString(ACCESS_TOKEN, "");
    }

    public String getSecretAccessToken() {
        return mPrefs.getString(ACCESS_TOKEN_SECRET, "");
    }

    public String getUserName() {
        return mPrefs.getString(NAME, "");
    }

    public String getImageUrl() {
        return mPrefs.getString(URL, "");
    }


    public void setTwitterToken(String token, String tokenSecret, String name, String url) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.putString(ACCESS_TOKEN_SECRET, tokenSecret);
        editor.putString(NAME, name);
        editor.putString(URL, url);
        editor.commit();

    }


}


