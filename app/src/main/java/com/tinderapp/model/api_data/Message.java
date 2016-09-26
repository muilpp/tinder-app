package com.tinderapp.model.api_data;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable{
    private String to;
    private String from;
    private String message;
    private long timestamp;

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
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    protected Message(Parcel in) {
        to = in.readString();
        from = in.readString();
        message = in.readString();
        timestamp = in.readLong();
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(to);
        parcel.writeString(from);
        parcel.writeString(message);
        parcel.writeLong(timestamp);
    }
}