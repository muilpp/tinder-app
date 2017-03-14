package com.tinderapp.helper.model;

import com.tinderapp.model.BaseApplication;
import com.tinderapp.model.apidata.Message;

import org.joda.time.DateTime;

import java.util.List;

public class MessageHelper {

    private MessageHelper() {}

    public static void addNewMessageToChat(String matchID, List<Message> messageList, String userID, String lastActivityDate) {
        if (matchID.contains(userID)) {
            for (Message message : messageList) {
                //Message is sent by our match and received after last check, so we add it to the list
                if (message.getFrom().equalsIgnoreCase(userID) && new DateTime(message.getTimestamp()).isAfter(new DateTime(lastActivityDate))) {
                    BaseApplication.getEventBus().post(new Message(message.getTo(), message.getFrom(), message.getMessageText(), DateTime.now().getMillis()));
                }
            }
        }
    }
}
