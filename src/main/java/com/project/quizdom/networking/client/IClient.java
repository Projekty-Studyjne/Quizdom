package com.project.quizdom.networking.client;

import java.io.IOException;

public interface IClient {
    void sendReady(boolean ready) throws IOException;
    void sendClose() throws IOException;

    void sendCategory(String category) throws IOException;
    void sendScore(int score) throws IOException;
    void sendEnding() throws IOException;

}
