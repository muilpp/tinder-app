package com.tinderapp.model.api_data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MatchDTO {
    @SerializedName("matches")
    private List<Match> matchList;
    @SerializedName("blocks")
    private List<String> blocks;

    public List<Match> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<Match> matchList) {
        this.matchList = matchList;
    }

    public List<String> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<String> blocks) {
        this.blocks = blocks;
    }
}