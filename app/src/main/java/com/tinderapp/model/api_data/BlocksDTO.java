package com.tinderapp.model.api_data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BlocksDTO {
    @SerializedName("blocks")
    private List<String> blockList;

    public List<String> getBlockList() {
        return blockList;
    }

    public void setBlockList(List<String> blockList) {
        this.blockList = blockList;
    }
}
