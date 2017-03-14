package com.tinderapp.model.apidata;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Instagram implements Parcelable{

    @SerializedName("media_count")
    private int mediaCount;
    private String username;
    public static final Creator<Instagram> CREATOR = new Creator<Instagram>() {
        @Override
        public Instagram createFromParcel(Parcel in) {
            return new Instagram(in);
        }

        @Override
        public Instagram[] newArray(int size) {
            return new Instagram[size];
        }
    };

    protected Instagram(Parcel in) {
        mediaCount = in.readInt();
        username = in.readString();
    }

    public int getMediaCount() {
        return mediaCount;
    }
    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mediaCount);
        parcel.writeString(username);
    }
}