package com.project.quizdom.model;

import java.net.InetAddress;

public class User {
    private String nickname;
    private boolean isReady;
    private InetAddress address;

    public User(String nickname) {
        this.nickname = nickname;
        this.isReady = false;
    }

    public User(String nickname, InetAddress address) {
        this.nickname = nickname;
        this.address = address;
        this.isReady = false;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isReady() {
        return isReady;
    }
}
