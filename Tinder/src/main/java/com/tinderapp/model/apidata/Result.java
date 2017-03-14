package com.tinderapp.model.apidata;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result implements Parcelable{
    @SerializedName("_id")
    private String id;

    @SerializedName("birth_date")
    private String birthDate;
    private String name;
    private String bio;

    @SerializedName("distance_mi")
    private String distance;

    @SerializedName("connection_count")
    private String connectionCount;

    @SerializedName("ping_time")
    private String lastConnection;

    private List<Photo> photos;
    private Instagram instagram;

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    protected Result(Parcel in) {
        id = in.readString();
        birthDate = in.readString();
        name = in.readString();
        bio = in.readString();
        distance = in.readString();
        connectionCount = in.readString();
        lastConnection = in.readString();
        photos = in.createTypedArrayList(Photo.CREATOR);
        instagram = in.readParcelable(Instagram.class.getClassLoader());
    }

    public void setLastConnection(String lastConnection) {
        this.lastConnection = lastConnection;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public void setInstagram(Instagram instagram) {
        this.instagram = instagram;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Photo> getPhotos() {
        return photos;
    }
    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    @SerializedName("distance_mi")
    public String getDistance() {
        return distance;
    }
    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getLastConnection() {
        return lastConnection;
    }

    public Instagram getInstagram() {
        return instagram;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Result result = (Result) o;

        return id != null ? id.equals(result.id) : result.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(birthDate);
        parcel.writeString(name);
        parcel.writeString(bio);
        parcel.writeString(distance);
        parcel.writeString(connectionCount);
        parcel.writeString(lastConnection);
        parcel.writeTypedList(photos);
        parcel.writeParcelable(instagram, i);
    }
}