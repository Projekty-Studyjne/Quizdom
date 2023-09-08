package com.project.quizdom.model;

import java.awt.*;
import java.io.Serializable;

public class Message implements Serializable {
    private MessageType msgType;
    private String nickname;
    private String content;

    public Message(MessageType msgType, String nickname, String content) {
        this.msgType = msgType;
        this.nickname = nickname;
        this.content = content;
    }

    public Message() {
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
