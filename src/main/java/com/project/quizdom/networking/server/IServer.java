package com.project.quizdom.networking.server;

import java.io.IOException;

public interface IServer {
    public boolean checkCanStartGame();
    public void sendClose() throws IOException;
}
