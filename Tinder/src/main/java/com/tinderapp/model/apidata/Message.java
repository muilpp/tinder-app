package com.tinderapp.model.apidata;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Message implements Parcelable{
    private String to;
    private String from;
    @SerializedName("message")
    private String messageText;
    private long timestamp;

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public Message(String to, String from, String messageText, long timestamp) {
        this.to = to;
        this.from = from;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    protected Message(Parcel in) {
        to = in.readString();
        from = in.readString();
        messageText = in.readString();
        timestamp = in.readLong();
    }

    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getMessageText() {
        return messageText;
    }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(to);
        parcel.writeString(from);
        parcel.writeString(messageText);
        parcel.writeLong(timestamp);
    }
}