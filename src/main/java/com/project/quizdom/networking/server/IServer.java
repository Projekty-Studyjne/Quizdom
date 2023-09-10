package com.project.quizdom.networking.server;

import java.io.IOException;

public interface IServer {
    public boolean checkCanStartGame();
    public void sendClose() throws IOException;
    public void sendStartGame() throws IOException;
    public void sendCategory(String category) throws IOException;
    public void startQuiz() throws IOException;
}
