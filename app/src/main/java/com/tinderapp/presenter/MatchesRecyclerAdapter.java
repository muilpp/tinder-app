package com.tinderapp.presenter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.api_data.Match;
import com.tinderapp.model.api_data.Message;
import com.tinderapp.view.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class MatchesRecyclerAdapter extends RecyclerView.Adapter<MatchesRecyclerAdapter.MatchViewHolder> {
    private List<Match> matchList;
    private String mUserToken;
    private Activity mActivity;

    public MatchesRecyclerAdapter(List<Match> matchList, Activity parentActivity, String userToken) {
        this.matchList = matchList;
        mUserToken = userToken;
        mActivity = parentActivity;
    }

    public class MatchViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView matchName;
        TextView lastMessage;
        ImageView matchPhoto;

        MatchViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.match_card_view);
            matchName = (TextView) itemView.findViewById(R.id.person_name);
            lastMessage = (TextView) itemView.findViewById(R.id.last_message);
            matchPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
        }
    }

    public void updateData(List<Match> matchList) {
        this.matchList = matchList;
    }

    @Override
    public MatchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_card_view, parent, false);
        return new MatchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MatchViewHolder holder, final int position) {
        holder.matchName.setText(matchList.get(position).getPerson().getName());
        final ArrayList<Message> messageList = new ArrayList<>(matchList.get(position).getMessageList());

        if (messageList.size() > 0) {
            holder.lastMessage.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.lastMessage.getLayoutParams();
            DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();

            //The match sent the last message, show the left bubble
            if (matchList.get(position).getPerson().getId().equals(messageList.get(messageList.size()-1).getFrom())) {
                holder.lastMessage.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.bubble_left));
                params.addRule(RelativeLayout.END_OF, R.id.person_photo);
                params.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
            } else {
                holder.lastMessage.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.bubble_right));
                params.removeRule(RelativeLayout.END_OF);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);
                holder.lastMessage.setMaxWidth(displayMetrics.widthPixels / 2);
            }
            holder.lastMessage.setText(messageList.get(messageList.size()-1).getMessage());
        } else {
            holder.lastMessage.setVisibility(View.GONE);
            holder.lastMessage.setText("");
        }

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatActivityIntent = new Intent(mActivity, ChatActivity.class);
                chatActivityIntent.putParcelableArrayListExtra(BuildConfig.MESSAGE_LIST, messageList);
                chatActivityIntent.putExtra(BuildConfig.USER_ID, matchList.get(position).getPerson().getId());
                chatActivityIntent.putExtra(BuildConfig.MATCH_ID, matchList.get(position).getId());
                chatActivityIntent.putExtra(BuildConfig.USER_IMAGE, matchList.get(position).getPerson().getPhotoList().get(0).getProcessedFiles().get(0).getUrl());
                chatActivityIntent.putExtra(BuildConfig.USER_TOKEN, mUserToken);
                mActivity.startActivity(chatActivityIntent);
            }
        });
        Picasso.with(mActivity)
                .load(matchList.get(position).getPerson().getPhotoList().get(0).getProcessedFiles().get(0).getUrl())
                .into(holder.matchPhoto);
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }
}
