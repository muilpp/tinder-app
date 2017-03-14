package com.tinderapp.model.apidata;

import com.google.gson.annotations.SerializedName;

public class FacebookTokenDTO {
    @SerializedName("facebook_token")
    private String facebookToken;

    public String getFacebookToken() {
        return facebookToken;
    }

    public void setFacebookToken(String facebookToken) {
        this.facebookToken = facebookToken;
    }
}