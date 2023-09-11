package com.project.quizdom.networking.server;

import java.io.IOException;

public interface IServer {
    boolean checkCanStartGame();
    void sendClose() throws IOException;
    void sendStartGame() throws IOException;
    void sendCategory(String category) throws IOException;
    void sendScore(int score) throws IOException;
    void sendEnding() throws IOException;
}
