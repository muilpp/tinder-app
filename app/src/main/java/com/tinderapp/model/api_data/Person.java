package com.tinderapp.model.api_data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Person {
    @SerializedName("photos")
    private List<Photo> photoList;
    private String name, birth_date, bio;

    @SerializedName("_id")
    public String id;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<Photo> getPhotoList() {
        return photoList;
    }
    public void setPhotoList(List<Photo> photoList) {
        this.photoList = photoList;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getBirthDate() {
		return birth_date;
	}
	public void setBirthDate(String birth_date) {
		this.birth_date = birth_date;
	}

	public String getBio() {
		return bio;
	}
	public void setBio(String bio) {
		this.bio = bio;
	}

}