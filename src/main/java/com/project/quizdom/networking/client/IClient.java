package com.project.quizdom.networking.client;

import java.io.IOException;

public interface IClient {
    public void sendReady(boolean ready) throws IOException;
    public void sendClose() throws IOException;
}
