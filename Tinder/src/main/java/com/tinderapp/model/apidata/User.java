package com.tinderapp.model.apidata;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    private String name;
    @SerializedName("_id")
    private String id;
    @SerializedName("photos")
    private List<Photo> photoList;

    public List<Photo> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(List<Photo> photoList) {
        this.photoList = photoList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}