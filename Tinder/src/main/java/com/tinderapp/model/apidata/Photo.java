package com.tinderapp.model.apidata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Photo implements Parcelable{
    private List<ProcessedFiles> processedFiles;

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    protected Photo(Parcel in) {
        processedFiles = in.createTypedArrayList(ProcessedFiles.CREATOR);
    }

    public List<ProcessedFiles> getProcessedFiles() {
        return processedFiles;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(processedFiles);
    }
}