package com.project.quizdom.networking.client;

import com.project.quizdom.game.Controller;
import com.project.quizdom.model.Message;
import com.project.quizdom.model.MessageType;
import com.project.quizdom.model.User;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements IClient {
    private final Controller controller;
    private final String nickname;
    private ObjectOutputStream output;

    public Client(Controller controller, String address, int port, String nickname) {
        this.controller = controller;
        this.nickname = nickname;
        ClientListener clientListener = new ClientListener(address, port);
        clientListener.start();
    }

    private void sendMessage(Message message) throws IOException {
        this.output.writeObject(message);
    }

    @Override
    public void sendReady(boolean ready) throws IOException {
        Message message = new Message(MessageType.READY, this.nickname, "" + ready);
        this.sendMessage(message);
    }

    @Override
    public void sendClose() throws IOException {
        Message message = new Message(MessageType.DISCONNECT, this.nickname, "");
        this.sendMessage(message);
    }

    private List<User> extractUserList(String s) {
        List<User> list = new ArrayList<User>();
        String[] temp = s.split(";");
        for (int i = 0; i < temp.length; i++) {
            String[] nickReady = temp[i].split(",");
            User user = new User(nickReady[0]);
            user.setReady(Boolean.parseBoolean(nickReady[1]));
            list.add(user);
        }
        return list;
    }

    private class ClientListener extends Thread {
        private final String address;
        private final int port;

        public ClientListener(String address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(address, port);
                OutputStream os = socket.getOutputStream();
                output = new ObjectOutputStream(os);
                InputStream is = socket.getInputStream();
                ObjectInputStream input = new ObjectInputStream(is);
                Message message = new Message(MessageType.CONNECT, nickname, "");
                output.writeObject(message);
                while (socket.isConnected()) {
                    Message incomingMessage = (Message) input.readObject();
                    if (incomingMessage != null) {
                        switch (incomingMessage.getMsgType()) {
                            case CONNECT_FAILED: {
                                controller.showConnectingBox(false);
                                break;
                            }
                            case CONNECT_OK: {
                                controller.showConnectingBox(false);
                                controller.switchToClientRoom();
                                controller.resetList();
                                controller.updateUserList(extractUserList(incomingMessage.getContent()));
                                break;
                            }
                            case USER_JOINED: {
                                controller.addUser(new User(incomingMessage.getNickname()));
                                break;
                            }
                            case READY: {
                                controller.updateReady(incomingMessage.getNickname(), Boolean.parseBoolean(incomingMessage.getContent()));
                            }
                            case DISCONNECT: {
                                if (incomingMessage.getNickname().equals(nickname)) {
                                    controller.switchToMP();
                                } else {
                                    controller.removeUser(incomingMessage.getNickname());
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
