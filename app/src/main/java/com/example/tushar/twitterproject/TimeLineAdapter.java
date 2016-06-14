package com.example.tushar.twitterproject;

/**
 * Created by tushar on 11/6/16.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import twitter4j.Status;


public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.ViewHolder> {
    private static ArrayList<Status> productEntries;
    private static Context context;
    private static int currentPage = 1;

    public TimeLineAdapter(ArrayList<Status> ProductEntry, Context context) {
        this.productEntries = ProductEntry;
        this.context = context;
    }

    @Override
    public TimeLineAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_card_view, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TimeLineAdapter.ViewHolder holder, int position) {
        twitter4j.Status productEntry = productEntries.get(position);
        holder.postDate.setText(getFormattedTime(productEntry.getCreatedAt()));
        holder.postText.setText(productEntry.getText());
        holder.userName.setText(productEntry.getUser().getName());
        Picasso.with(context).load(productEntry.getUser().getProfileImageURL()).into(holder.productImage);
    }

    private String getFormattedTime(Date createdAt) { //displays the difference of current time and
        Calendar c = Calendar.getInstance();             //time of tweet
        Date d = c.getTime();
        long millis = d.getTime() - createdAt.getTime();
        long sec = millis / 1000;
        int min = (int) sec / 60;
        int Hours = min / 60;
        int days = Hours / 24;
        min = min % 60;
        if (days > 0) {
            return days + " day ago";
        } else if (Hours > 0) {
            return Hours + " hour ago";
        }
        if (min == 0) {
            return "just now";
        } else {
            return min + " min ago";
        }

    }

    @Override
    public int getItemCount() {
        return productEntries.size();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void incCurrentPage() {
        currentPage++;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView productImage;
        public TextView userName;
        public TextView postDate;
        public TextView postText;

        public ViewHolder(View view) {
            super(view);
            productImage = (ImageView) view.findViewById(R.id.product_image);
            userName = (TextView) view.findViewById(R.id.user_name);
            postDate = (TextView) view.findViewById(R.id.post_time);
            postText = (TextView) view.findViewById(R.id.story_text);


        }
    }
}
