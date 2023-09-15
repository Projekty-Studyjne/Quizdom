package com.project.quizdom.networking.client;

import com.project.quizdom.game.Controller;
import com.project.quizdom.model.Message;
import com.project.quizdom.model.MessageType;
import com.project.quizdom.model.User;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
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

    @Override
    public void sendCategory(String category) throws IOException {
        Message message = new Message(MessageType.CATEGORY, this.nickname, category);
        this.sendMessage(message);
    }

    @Override
    public void sendCorrectAnswer(int score) throws IOException {
        Message message = new Message(MessageType.QUIZ, this.nickname, String.valueOf(score));
        this.sendMessage(message);

    }

    @Override
    public void sendWrongAnswer(int score) throws IOException {
        Message message = new Message(MessageType.QUIZ, this.nickname, String.valueOf(score));
        this.sendMessage(message);
    }

    @Override
    public void sendEnding() throws IOException {
        Message message = new Message(MessageType.END, this.nickname, "");
        this.sendMessage(message);
    }

    @Override
    public void sendPlayAgain() throws IOException {
        Message message = new Message(MessageType.PLAY_AGAIN, this.nickname, "");
        this.sendMessage(message);
    }

    private List<User> extractUserList(String s) {
        List<User> list = new ArrayList<>();
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
                                controller.setServerNickname(incomingMessage.getNickname());
                                break;
                            }
                            case READY: {
                                controller.updateReady(incomingMessage.getNickname(), Boolean.parseBoolean(incomingMessage.getContent()));
                                break;
                            }
                            case START_GAME: {
                                controller.startGame();
                                break;
                            }
                            case QUESTIONS: {
                                controller.setDrawQuestions(Integer.valueOf(incomingMessage.getContent()));
                                break;
                            }
                            case CATEGORY: {
                                controller.setCategory(incomingMessage.getContent());
                                controller.switchToQuiz();
                                break;
                            }
                            case QUIZ: {
                                controller.setScore(incomingMessage.getContent());
                                break;
                            }
                            case END: {
                                controller.switchToEnding();
                                break;
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
            } catch (SocketException e) {
                System.out.println("Socket exception");
                if (e instanceof ConnectException) {
                    controller.showConnectingBox(false);
                } else if (e.getMessage().equals("Connection reset")) {
                    System.out.println("Stream closed");
                } else e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Stream closed");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}
