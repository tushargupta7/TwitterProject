package com.example.tushar.twitterproject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class MainActivity extends AppCompatActivity {
    private ImageView login;
    private Twitter twitter;
    private RequestToken requestToken = null;
    private AccessToken accessToken;
    private String oAuthUrl, oAuthVerify;
    private Dialog authDialog;
    private WebView webview;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        checkForAutoLogin();
    }

    private void init() {
        login = (ImageView) findViewById(R.id.login);
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
        login.setOnClickListener(new LoginProcess());
    }

    private void checkForAutoLogin() {  //Autologin if accesstoken is saved in preferences
        if (!Preferences.getInstance(MainActivity.this).getAccessToken().equalsIgnoreCase("")) {
            launchProfileActivity();
        } else return;
    }


    private class LoginProcess implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new FetchToken().execute();
        }
    }

    private class FetchToken extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                requestToken = twitter.getOAuthRequestToken();
                oAuthUrl = requestToken.getAuthorizationURL();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return oAuthUrl;
        }

        @Override
        protected void onPostExecute(String oauth_url) {
            if (oauth_url != null) {
                Log.e("URL", oauth_url);
                authDialog = new Dialog(MainActivity.this);
                authDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                authDialog.setContentView(R.layout.auth_dialog);
                webview = (WebView) authDialog.findViewById(R.id.webv);
                webview.getSettings().setJavaScriptEnabled(true);
                webview.loadUrl(oauth_url);
                webview.setWebViewClient(new WebViewClient() {
                    boolean authComplete = false;

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if (url.contains(getString(R.string.verify_auth)) && authComplete == false) {
                            authComplete = true;
                            Log.e("Url", url);
                            Uri uri = Uri.parse(url);
                            oAuthVerify = uri.getQueryParameter(getString(R.string.verify_auth));
                            authDialog.dismiss();
                            new FetchAccessToken().execute();
                        } else if (url.contains(getString(R.string.denied))) {
                            authDialog.dismiss();
                            Toast.makeText(MainActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                authDialog.show();
                authDialog.setCancelable(true);
            } else {

                Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class FetchAccessToken extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage(getString(R.string.fetch_data));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

        }


        @Override
        protected Boolean doInBackground(String... args) {
            try {
                accessToken = twitter.getOAuthAccessToken(requestToken, oAuthVerify);
                Preferences prefs = Preferences.getInstance(MainActivity.this);
                User user = twitter.showUser(accessToken.getUserId());
                prefs.setTwitterToken(accessToken.getToken(), accessToken.getTokenSecret(), user.getName(), user.getOriginalProfileImageURL());
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if (response) {
                progress.dismiss();
                launchProfileActivity();
            }

        }


    }

    private void launchProfileActivity() {
        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
