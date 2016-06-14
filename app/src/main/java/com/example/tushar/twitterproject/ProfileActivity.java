package com.example.tushar.twitterproject;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private TextView prof_name;
    private Bitmap bitmap;
    private ImageView postTweet;
    private EditText tweet_text;
    private ProgressDialog progress;
    private String tweetText;
    private RecyclerView recyclerView;
    private TimeLineAdapter timeLineAdapter;
    private final int START_PAGE = 1;
    private ArrayList<Status> timeLineList;
    private String userName;
    private String profPicUrl;
    private ImageView profImage;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        new UserTwitterProfileInfo().execute();
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) { //To handle lazy loading i.e fetching more data from server on scroll
                int currentPage = timeLineAdapter.getCurrentPage();
                HomeTimeLine load = new HomeTimeLine();
                load.execute(currentPage);
                timeLineAdapter.incCurrentPage();
            }
        });
        postTweet.setOnClickListener(this);


    }

    private void init() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        prof_name = (TextView) findViewById(R.id.prof_name);
        profImage = (ImageView) findViewById(R.id.prof_image);
        userName = Preferences.getInstance(ProfileActivity.this).getUserName();
        profPicUrl = Preferences.getInstance(ProfileActivity.this).getImageUrl();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        postTweet = (ImageView) findViewById(R.id.tweet);
        tweet_text = (EditText) findViewById(R.id.tweet_text);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tweet:
                new PostTweet().execute();
                break;
        }
    }

    @Override
    public void onRefresh() { //For on pull down refresh
        timeLineList.clear();
        timeLineAdapter.notifyDataSetChanged();
        new HomeTimeLine().execute(START_PAGE);
    }

    private class HomeTimeLine extends AsyncTask<Integer, Void, ResponseList<Status>> {

        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Integer... params) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants.CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants.CONSUMER_SECRET);
            int pageNo = params[0];                                     //Get Details by page
            twitter4j.Paging page = new Paging(pageNo);
            AccessToken accessToken = new AccessToken(Preferences.getInstance(ProfileActivity.this).getAccessToken(), Preferences.getInstance(ProfileActivity.this).getSecretAccessToken());
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            try {
                ResponseList<twitter4j.Status> statuses = twitter.getHomeTimeline(page);
                return statuses;
            } catch (TwitterException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            createCardViews(statuses);
        }
    }

    private void createCardViews(ResponseList<Status> statuses) {
        if (statuses == null) {
            Toast.makeText(ProfileActivity.this, "Please relogin", Toast.LENGTH_SHORT);
            return;
        }

        if (timeLineAdapter == null) {
            timeLineList.addAll(statuses);
            timeLineAdapter = new TimeLineAdapter(timeLineList, this);
            timeLineAdapter.incCurrentPage();
            recyclerView.setAdapter(timeLineAdapter);
            swipeRefreshLayout.setRefreshing(false);
        } else {
            int count = timeLineList.size();
            timeLineList.addAll(count, statuses);
            timeLineAdapter.notifyItemRangeInserted(count, timeLineList.size() - 1);
            timeLineAdapter.incCurrentPage();
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    private class PostTweet extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(ProfileActivity.this);
            progress.setMessage(getString(R.string.posting_tweet));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            tweetText = tweet_text.getText().toString();
            progress.show();

        }

        protected String doInBackground(String... args) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants.CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants.CONSUMER_SECRET);
            AccessToken accessToken = new AccessToken(Preferences.getInstance(ProfileActivity.this).getAccessToken(), Preferences.getInstance(ProfileActivity.this).getSecretAccessToken());
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

            try {
                twitter4j.Status response = twitter.updateStatus(tweetText);
                return response.toString();
            } catch (TwitterException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(String res) {
            if (res != null) {
                progress.dismiss();
                Toast.makeText(ProfileActivity.this, R.string.tweet_success, Toast.LENGTH_SHORT).show();
                tweet_text.setText(null);
            } else {
                progress.dismiss();
                Toast.makeText(ProfileActivity.this, R.string.tweet_fail, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private class UserTwitterProfileInfo extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(ProfileActivity.this);
            progress.setMessage(getString(R.string.profile_loading));
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

        }

        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(profPicUrl).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            Bitmap image_circle = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888); //Making image display circular
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setShader(shader);
            Canvas c = new Canvas(image_circle);
            c.drawCircle(image.getWidth() / 2, image.getHeight() / 2, image.getWidth() / 2, paint);
            profImage.setImageBitmap(image_circle);
            prof_name.setText("Welcome " + userName);
            progress.dismiss();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logout) {
            showLogoutDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_twitter)
                .setTitle(getString(R.string.twitter_logout))
                .setMessage(getString(R.string.logout_confirm))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogOut();
                    }

                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    private void LogOut() {
        Preferences.getInstance(ProfileActivity.this).setTwitterToken("", "", "", "");
        Intent signOutIntent = new Intent(ProfileActivity.this, MainActivity.class);
        signOutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(signOutIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeLineAdapter = null;
        timeLineList = new ArrayList<Status>();
        new HomeTimeLine().execute(START_PAGE);
    }

}
