package com.tinderapp.model.api_data;

import com.google.gson.annotations.SerializedName;

public class UserProfileDTO {
    private int status;

    @SerializedName("results")
    private Result result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}