package com.example.tushar.twitterproject;

import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class SearchResultsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TimeLineAdapter timeLineAdapter=null;
    private ArrayList<Status> timeLineList;
    private String query = null;
    private Query searchQuery;
    private boolean isFirstQuery = true;
    private boolean finishedLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        init();
        initRecylerView();
        handleIntent(getIntent());
    }

    private void initRecylerView() {
        recyclerView = (RecyclerView) findViewById(R.id.new_search_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (finishedLoading) {
                    return;
                }
                LoadSearchResults load = new LoadSearchResults();
                load.execute();
            }
        });
    }

    private void init() {
        timeLineList = new ArrayList<Status>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            new LoadSearchResults().execute();

        }
    }


    private class LoadSearchResults extends AsyncTask<Void, Void, List<Status>> {

        @Override
        protected List<twitter4j.Status> doInBackground(Void... params) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(Constants.CONSUMER_KEY);
            builder.setOAuthConsumerSecret(Constants.CONSUMER_SECRET);
            AccessToken accessToken = new AccessToken(Preferences.getInstance(SearchResultsActivity.this).getAccessToken(), Preferences.getInstance(SearchResultsActivity.this).getSecretAccessToken());
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
            if (isFirstQuery) {
                searchQuery = new Query(query);
                isFirstQuery = false;//For first time
            }
            try {
                QueryResult queryResult = twitter.search(searchQuery);
                searchQuery = queryResult.nextQuery();
                if (searchQuery == null) {
                    finishedLoading = true;
                }
                List<twitter4j.Status> statuses = queryResult.getTweets();
                return statuses;
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> statuses) {
            createCardViews(statuses);
        }
    }

    private void createCardViews(List<Status> statuses) {

        if (timeLineAdapter == null) {
            timeLineList.addAll(statuses);
            timeLineAdapter = new TimeLineAdapter(timeLineList, SearchResultsActivity.this);
            recyclerView.setAdapter(timeLineAdapter);
        } else {
            int count = timeLineList.size();
            timeLineList.addAll(count, statuses);
            timeLineAdapter.notifyItemRangeInserted(count, timeLineList.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        timeLineList.clear();
        timeLineAdapter.notifyDataSetChanged();
        super.onDestroy();
    }
}
