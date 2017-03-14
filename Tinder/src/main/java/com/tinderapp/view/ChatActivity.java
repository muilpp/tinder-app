package com.tinderapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.tinderapp.helper.model.MessageHelper;
import com.tinderapp.model.BaseApplication;
import com.tinderapp.model.TinderAPI;
import com.tinderapp.model.TinderRetrofit;
import com.tinderapp.model.adapter.CircleTransform;
import com.tinderapp.model.apidata.LastActivityDTO;
import com.tinderapp.model.apidata.Match;
import com.tinderapp.model.apidata.MatchDTO;
import com.tinderapp.model.apidata.Message;
import com.tinderapp.model.apidata.UserProfileDTO;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getName();
    private ArrayList<Message> mMessageList;
    private ImageView mUserImage;
    private ChatRecyclerAdapter mAdapter;
    private String mMatchID;
    private String mUserID;
    private String mLastActivityDate;
    private TinderAPI tinderAPI;

    private Handler handler;
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
                                MessageHelper.addNewMessageToChat(match.getId(), match.getMessageList(), mUserID, mLastActivityDate);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        handler = new Handler();

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

    @Subscribe
    public void onMessageReceived(Message message) {
        mMessageList.add(message);
        mAdapter.notifyItemInserted(mMessageList.size()-1);
    }

    public void initViews() {
        mMessageList = getIntent().getParcelableArrayListExtra(BuildConfig.MESSAGE_LIST);
        mUserID = getIntent().getStringExtra(BuildConfig.USER_ID);
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

        tinderAPI = new TinderRetrofit().getTokenInstanceWithContentType();
    }

    public void getUserInformation() {
        TinderAPI tokenTinderAPI = new TinderRetrofit().getTokenInstance();
        Call<ResponseBody> call = tokenTinderAPI.getUserProfile(mUserID);
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
                        final DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM dd HH:mm");
                        DateTime departureTime = new DateTime(lastTime.getMillis()).toDateTime(DateTimeZone.UTC);
                        lastConnTv.setText(departureTime.toString(fmt)+"h");

                        mUserImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
                                intent.putExtra(BuildConfig.USER_PROFILE, userProfileDTO.getResult());
                                intent.putExtra(BuildConfig.HIDE_LIKE_ICONS, true);
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
                itemHolder.messageContent.setText(mMessageList.get(position).getMessageText());
                DateTime messageTime = new DateTime(mMessageList.get(position).getTimestamp());

                DateTime departureTime = new DateTime(messageTime.getMillis()).toDateTime(DateTimeZone.UTC);
                if (messageTime.getDayOfMonth() == DateTime.now().getDayOfMonth()) {
                    final DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
                    itemHolder.messageTime.setText(departureTime.toString(fmt)+"h");
                } else {
                    final DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM dd HH:mm");
                    itemHolder.messageTime.setText(departureTime.toString(fmt)+"h");
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
                            footerHolder.sendMessageView.setVisibility(View.GONE);
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
                                            Toast.makeText(ChatActivity.this, R.string.message_not_sent, Toast.LENGTH_SHORT).show();
                                            Log.i(TAG, response.errorBody().string());
                                        }
                                    } catch (IOException e) {
                                        Log.e(TAG, e.getMessage(), e);
                                        Toast.makeText(ChatActivity.this, R.string.message_not_sent, Toast.LENGTH_SHORT).show();
                                    }
                                    footerHolder.sendMessageView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Log.e(TAG, t.getMessage(), t);
                                    Toast.makeText(ChatActivity.this, R.string.message_not_sent, Toast.LENGTH_SHORT).show();
                                    footerHolder.sendMessageView.setVisibility(View.VISIBLE);
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