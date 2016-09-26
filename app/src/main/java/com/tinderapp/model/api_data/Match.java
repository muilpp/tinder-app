package com.tinderapp.model.api_data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Match implements Comparable<Match> {
    @SerializedName("_id")
    private String id;
    @SerializedName("is_super_like")
    private boolean isSuperLike;
    @SerializedName("messages")
    private List<Message> messageList;
    private Person person;

    public boolean isSuperLike() {
        return isSuperLike;
    }

    public void setSuperLike(boolean superLike) {
        isSuperLike = superLike;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int compareTo(Match otherMatch) {

        if (messageList.size() == 0 && otherMatch.messageList.size() == 0)
            return 0;
        else if (messageList.size() == 0)
            return 1;
        else if (otherMatch.messageList.size() == 0)
            return -1;
        else if (messageList.get(messageList.size()-1).getTimestamp() > otherMatch.getMessageList().get(otherMatch.getMessageList().size()-1).getTimestamp())
            return -1;
        else if (messageList.get(messageList.size()-1).getTimestamp() < otherMatch.getMessageList().get(otherMatch.getMessageList().size()-1).getTimestamp())
            return 1;
        return 0;
    }
}