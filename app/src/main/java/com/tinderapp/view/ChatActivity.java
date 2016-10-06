package com.tinderapp.view;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;
import com.tinderapp.model.BaseApplication;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.api_data.LastActivityDTO;
import com.tinderapp.model.api_data.Match;
import com.tinderapp.model.api_data.MatchDTO;
import com.tinderapp.model.api_data.Message;
import com.tinderapp.model.api_data.Photo;
import com.tinderapp.model.api_data.UserProfileDTO;
import com.tinderapp.presenter.CircleTransform;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private final static String TAG = ChatActivity.class.getName();
    private ArrayList<Message> mMessageList;
    private ImageView mUserImage;
    private ChatRecyclerAdapter mAdapter;
    private String mUserToken, mMatchID, mUserID, mLastActivityDate;
    private TinderAPI tinderAPI;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        handler = new Handler();
//        handler.postDelayed(timerThread, 2000);

        initViews();

        //Add 10 minutes to offset the difference with the server
        mLastActivityDate = DateTime.now().plusMinutes(10).toString();

    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseApplication.getEventBus().register(this);
        handler.postDelayed(timerThread, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseApplication.getEventBus().unregister(this);
        handler.removeCallbacks(timerThread);
    }

    private Runnable timerThread = new Runnable() {
        @Override
        public void run() {
            LastActivityDTO lastActivityDTO = new LastActivityDTO();
            lastActivityDTO.setLastActivityDate(mLastActivityDate);
            Call<ResponseBody> call = tinderAPI.getUpdates(lastActivityDTO);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String responseStr = response.body().string();
                        MatchDTO matches = new Gson().fromJson(responseStr, MatchDTO.class);

                        if (matches.getMatchList() != null) {

                            for (Match match : matches.getMatchList()) {
                                //Fetch only the messages of the current user
                                if (match.getId().contains(mUserID)) {
                                    for (Message message : match.getMessageList()) {
                                        //Message is received after last check, so we add it to the list
                                        if (new DateTime(message.getTimestamp()).isAfter(new DateTime(mLastActivityDate))) {
                                            BaseApplication.getEventBus().post(new Message(message.getTo(), message.getFrom(), message.getMessage(), DateTime.now().getMillis()));
                                        }
                                    }
                                }
                            }
                        }

                        mLastActivityDate = matches.getLastActivity();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, t.getMessage(), t);
                }
            });

            handler.postDelayed(this, 3000);
        }
    };

    @Subscribe
    public void onMessageReceived(Message message) {
        mMessageList.add(message);
        mAdapter.notifyItemInserted(mMessageList.size()-1);
    }

    public void initViews() {
        mMessageList = getIntent().getParcelableArrayListExtra(BuildConfig.MESSAGE_LIST);
        mUserID = getIntent().getStringExtra(BuildConfig.USER_ID);
        mUserToken = getIntent().getStringExtra(BuildConfig.USER_TOKEN);
        mMatchID = getIntent().getStringExtra(BuildConfig.MATCH_ID);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);

        mAdapter = new ChatRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(mMessageList.size()-1);

        getUserInformation();

        mUserImage = (ImageView) findViewById(R.id.user_image);
        Picasso.with(this)
                .load(getIntent().getStringExtra(BuildConfig.USER_IMAGE))
                .resize(80, 80)
                .centerCrop()
                .transform(new CircleTransform())
                .into(mUserImage);

        tinderAPI = TinderRetrofit.getTokenInstanceWithContentType(mUserToken);
    }

    public void getUserInformation() {
        TinderAPI tinderAPI = TinderRetrofit.getTokenInstance(mUserToken);
        Call<ResponseBody> call = tinderAPI.getUserProfile(mUserID);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        final UserProfileDTO userProfileDTO = new Gson().fromJson(response.body().string(), UserProfileDTO.class);

                        TextView userNameTv = (TextView) findViewById(R.id.user_name);
                        userNameTv.setText(userProfileDTO.getResult().getName());

                        TextView lastConnTv = (TextView) findViewById(R.id.last_connection_time);
                        DateTime lastTime = new DateTime(userProfileDTO.getResult().getLastConnection());
                        lastConnTv.setText(lastTime.getYear()+"-"+lastTime.getMonthOfYear()+"-"+lastTime.getDayOfMonth()+" "+lastTime.getHourOfDay()+":"+lastTime.getMinuteOfHour()+"h");

                        mUserImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                                intent.putExtra(BuildConfig.USER_NAME, userProfileDTO.getResult().getName());
                                intent.putExtra(BuildConfig.BIO, userProfileDTO.getResult().getBio());
                                intent.putExtra(BuildConfig.BIRTH_DATE, userProfileDTO.getResult().getBirth_date());
                                intent.putExtra(BuildConfig.DISTANCE, userProfileDTO.getResult().getDistance());
                                intent.putExtra(BuildConfig.USER_ID, userProfileDTO.getResult().getId());
                                intent.putExtra(BuildConfig.USER_TOKEN, mUserToken);
                                intent.putExtra(BuildConfig.HIDE_LIKE_ICONS, true);
//                                    intent.putExtra(BuildConfig.SCHOOL, userProfile.getResult().get);
                                if (userProfileDTO.getResult().getInstagram() != null)
                                    intent.putExtra(BuildConfig.IGRAM, userProfileDTO.getResult().getInstagram().getUsername());

                                intent.putExtra(BuildConfig.LAST_CONNECTION, userProfileDTO.getResult().getLastConnection());
//                                    intent.putExtra(BuildConfig.JOB, "");
//                                    intent.putExtra("url", userProfile.getResult().getPhotos().get(0).getProcessedFiles().get(0).getUrl());
                                //userProfile.getResult().getPhotos().get(0).getProcessedFiles().get(0).get

                                ArrayList<String> imageList = new ArrayList<>();
                                for (Photo photo : userProfileDTO.getResult().getPhotos()) {
                                    imageList.add(photo.getProcessedFiles().get(0).getUrl());
                                }

                                intent.putStringArrayListExtra(BuildConfig.IMAGES_ARRAY, imageList);
                                intent.putExtra(BuildConfig.MATCH_ID, getIntent().getStringExtra(BuildConfig.MATCH_ID));

                                startActivity(intent);
                            }
                        });
                    } else {
                        Log.i(TAG, response.errorBody().string());
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, t.getMessage(), t);
            }
        });
    }

    public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;

            if (viewType == TYPE_ITEM) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_view, parent, false);
                return new ChatViewItemHolder(v);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_view_footer, parent, false);
                return new ChatViewFooterHolder(v);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionFooter(position)) {
                return TYPE_FOOTER;
            }
            return TYPE_ITEM;
        }

        private boolean isPositionFooter (int position) {
            return position == mMessageList.size ();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ChatViewItemHolder) {
                ChatViewItemHolder itemHolder = (ChatViewItemHolder) holder;
                itemHolder.messageContent.setText(mMessageList.get(position).getMessage());
                DateTime messageTime = new DateTime(mMessageList.get(position).getTimestamp());

                if (messageTime.getDayOfMonth() == DateTime.now().getDayOfMonth()) {
                    itemHolder.messageTime.setText(messageTime.getHourOfDay()+":"+messageTime.getMinuteOfHour()+"h");
                } else {
                    itemHolder.messageTime.setText(messageTime.getYear() + "-" + messageTime.getMonthOfYear() + "-" + messageTime.getDayOfMonth() + " " + messageTime.getHourOfDay()+":"+messageTime.getMinuteOfHour()+"h");
                }

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemHolder.messageLayout.getLayoutParams();

                //if the match user is the sender, the right bubble chat is set
                if (mUserID.equals(mMessageList.get(position).getFrom())) {
                    itemHolder.messageLayout.setBackground(ContextCompat.getDrawable(ChatActivity.this, R.drawable.bubble_left));
                    params.gravity = Gravity.START;
                } else {
                    itemHolder.messageLayout.setBackground(ContextCompat.getDrawable(ChatActivity.this, R.drawable.bubble_right_blue));
                    params.gravity = Gravity.END;
                }

                itemHolder.messageLayout.setLayoutParams(params);
            } else {
                final ChatViewFooterHolder footerHolder = (ChatViewFooterHolder) holder;
                footerHolder.sendMessageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (footerHolder.messageEditText.getText().toString().length() > 0) {
                            MessageDTO messageDTO = new MessageDTO();
                            messageDTO.setMessage(footerHolder.messageEditText.getText().toString());
                            Call<ResponseBody> call = tinderAPI.sendMessage(mMatchID, messageDTO);
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {
                                        if (response.isSuccessful() && response.body() != null) {
                                            String responseStr = response.body().string();
                                            Message message = new Gson().fromJson(responseStr, Message.class);
                                            message.setTimestamp(DateTime.now().getMillis());
                                            mMessageList.add(message);
                                            mAdapter.notifyItemInserted(mMessageList.size()-1);
                                            footerHolder.messageEditText.setText("");
                                        } else {
                                            Log.i(TAG, response.errorBody().string());
                                        }
                                    } catch (IOException e) {
                                        Log.e(TAG, e.getMessage(), e);
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e(TAG, t.getMessage(), t);
                                }
                            });

                        } else Toast.makeText(ChatActivity.this, getString(R.string.empty_message), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            //Add 1 to count the footer view as well
            return mMessageList.size() + 1;
        }

        public class ChatViewItemHolder extends RecyclerView.ViewHolder {
            RelativeLayout messageLayout;
            TextView messageContent;
            TextView messageTime;

            ChatViewItemHolder(View itemView) {
                super(itemView);
                messageLayout = (RelativeLayout) itemView.findViewById(R.id.message_layout);
                messageContent = (TextView) itemView.findViewById(R.id.message_content);
                messageTime = (TextView) itemView.findViewById(R.id.message_time);
            }
        }

        public class ChatViewFooterHolder extends RecyclerView.ViewHolder {
            ImageView sendMessageView;
            EditText messageEditText;

            public ChatViewFooterHolder(View itemView) {
                super(itemView);
                sendMessageView = (ImageView) itemView.findViewById(R.id.send_message_view);
                messageEditText = (EditText) itemView.findViewById(R.id.input_message);
            }
//            RelativeLayout messageLayout;
//            TextView messageContent;
//            TextView messageTime;
//
//            ChatViewFooterHolder(View itemView) {
//                super(itemView);
//                messageLayout = (RelativeLayout) itemView.findViewById(R.id.message_layout);
//                messageContent = (TextView) itemView.findViewById(R.id.message_content);
//                messageTime = (TextView) itemView.findViewById(R.id.message_time);
//            }
        }
    }

    public class MessageDTO {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}