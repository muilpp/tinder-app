package com.tinderapp.model.apidata;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Person {
    @SerializedName("photos")
    private List<Photo> photoList;
    private String name;
    private String birthDate;
    private String bio;

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

    @SerializedName("birth_date")
    public String getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getBio() {
		return bio;
	}
	public void setBio(String bio) {
		this.bio = bio;
	}

}