package com.tinderapp.model.apidata;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecommendationsDTO {
    @SerializedName("results")
    private List<Result> result;

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }
}
