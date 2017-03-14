package com.tinderapp.model.apidata;

import android.os.Parcel;
import android.os.Parcelable;

public class ProcessedFiles implements Parcelable{
    private String url;
    private int height;
    private int width;

    public static final Creator<ProcessedFiles> CREATOR = new Creator<ProcessedFiles>() {
        @Override
        public ProcessedFiles createFromParcel(Parcel in) {
            return new ProcessedFiles(in);
        }

        @Override
        public ProcessedFiles[] newArray(int size) {
            return new ProcessedFiles[size];
        }
    };

    protected ProcessedFiles(Parcel in) {
        url = in.readString();
        height = in.readInt();
        width = in.readInt();
    }

    public String getUrl() {
        return url;
    }
    public int getHeight() {
        return height;
    }
    public int getWidth() {
        return width;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeInt(height);
        parcel.writeInt(width);
    }
}
