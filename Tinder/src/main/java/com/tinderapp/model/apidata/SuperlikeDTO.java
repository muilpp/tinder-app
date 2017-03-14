package com.tinderapp.model.apidata;

import com.google.gson.annotations.SerializedName;

public class SuperlikeDTO {
    private boolean match;
    private String status;

    @SerializedName("limit_exceeded")
    private boolean limitExceeded;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getMatch() {
        return match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public boolean getLimitExceeded() {
        return limitExceeded;
    }

    public void setLimitExceeded(boolean limitExceeded) {
        this.limitExceeded = limitExceeded;
    }
}