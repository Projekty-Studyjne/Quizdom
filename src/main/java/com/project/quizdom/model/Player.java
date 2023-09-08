package com.project.quizdom.model;

public class Player extends User {
    private int turn;

    public Player(String nickname, int turn) {
        super(nickname);
        this.turn = turn;
    }

}
