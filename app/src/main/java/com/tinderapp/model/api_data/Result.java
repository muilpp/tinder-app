package com.tinderapp.model.api_data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Result {
    @SerializedName("_id")
    private String id;
    private String birth_date;
    private String name;
    private String bio;

    @SerializedName("distance_mi")
    private String distance;
    private String connection_count;

    @SerializedName("ping_time")
    private String lastConnection;
    private List<Photo> photos;
    private Teaser teaser;
    private Instagram instagram;

    @SerializedName("badges")
    private List<Badges> badgesList;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getBirth_date() {
        return birth_date;
    }
    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
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
    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
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

    public Teaser getTeaser() {
        return teaser;
    }
    public void setTeaser(Teaser teaser) {
        this.teaser = teaser;
    }

    public String getConnection_count() {
        return connection_count;
    }
    public void setConnection_count(String connection_count) {
        this.connection_count = connection_count;
    }

    public String getLastConnection() {
        return lastConnection;
    }
    public void setLastConnection(String lastConnection) {
        this.lastConnection = lastConnection;
    }

    public Instagram getInstagram() {
        return instagram;
    }
    public void setInstagram(Instagram instagram) {
        this.instagram = instagram;
    }

    public List<Badges> getBadgesList() {
        return badgesList;
    }
    public void setBadgesList(List<Badges> badgesList) {
        this.badgesList = badgesList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Result result = (Result) o;

        return id != null ? id.equals(result.id) : result.id == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}