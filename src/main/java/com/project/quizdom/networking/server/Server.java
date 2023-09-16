package com.project.quizdom.networking.server;

import com.project.quizdom.game.Controller;
import com.project.quizdom.model.Message;
import com.project.quizdom.model.MessageType;
import com.project.quizdom.model.User;
import javafx.scene.control.Alert;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server implements IServer {
    private static final int PORT = 9001;
    private int maxNumUsers;
    private final Controller controller;
    private final String nickname;
    private final ArrayList<User> users;
    private final ServerListener serverListener;
    private final ArrayList<ObjectOutputStream> writers;

    public Server(Controller controller, String nickname, int usersRequired) throws IOException {
        this.controller = controller;
        this.nickname = nickname;
        this.maxNumUsers = usersRequired;
        this.users = new ArrayList<>();
        User user = new User(nickname);
        user.setReady(true);
        this.users.add(user);
        this.writers = new ArrayList<>();
        this.writers.add(null);
        this.serverListener = new ServerListener(PORT);
        this.serverListener.start();
        this.controller.switchToServerRoom();

    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public boolean checkCanStartGame() {
        for (User user : this.users) {
            if (!user.isReady()) {
                return false;
            }
        }
        return this.users.size() >= this.maxNumUsers;
    }

    @Override
    public void sendClose() throws IOException {
        Message message = new Message(MessageType.DISCONNECT, this.nickname, "Server room closed");
        for (int i = 1; i < this.users.size(); i++) {
            message.setNickname(this.users.get(i).getNickname());
            this.writers.get(i).writeObject(message);
        }
        serverListener.closeSocket();
    }

    @Override
    public void sendStartGame() throws IOException {
        Message message = new Message(MessageType.START_GAME, this.nickname, "START!");
        for (int i = 1; i < this.users.size(); i++) {
            message.setNickname(this.users.get(i).getNickname());
            this.writers.get(i).writeObject(message);
        }
    }

    @Override
    public void sendCategory(String category) throws IOException {
        Message message = new Message(MessageType.CATEGORY, this.nickname, category);
        for (int i = 1; i < this.users.size(); i++) {
            message.setNickname(this.users.get(i).getNickname());
            this.writers.get(i).writeObject(message);
        }
    }

    @Override
    public void sendQuestion(String question) throws IOException {
        Message message = new Message(MessageType.QUESTIONS, this.nickname, question);
        for (int i = 1; i < this.users.size(); i++) {
            message.setNickname(this.users.get(i).getNickname());
            this.writers.get(i).writeObject(message);
        }
    }

    @Override
    public void sendScore(int score) throws IOException {
        Message message = new Message(MessageType.QUIZ, this.nickname, String.valueOf(score));
        for (int i = 1; i < this.users.size(); i++) {
            message.setNickname(this.users.get(i).getNickname());
            this.writers.get(i).writeObject(message);
        }
    }

    @Override
    public void sendEnding() throws IOException {
        Message message = new Message(MessageType.END, this.nickname, "");
        for (int i = 1; i < this.users.size(); i++) {
            message.setNickname(this.users.get(i).getNickname());
            this.writers.get(i).writeObject(message);
        }
    }

    private String getUserList() {
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < this.users.size(); i++) {
            User user = this.users.get(i);
            list.append(user.getNickname()).append(",").append(user.isReady());
            list.append(i == this.users.size() - 1 ? "" : ";");
        }
        return list.toString();
    }

    private class ServerListener extends Thread {
        private final ServerSocket listener;

        public ServerListener(int port) throws IOException {
            this.listener = new ServerSocket(port);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    new Handler(this.listener.accept()).start();
                }
            } catch (SocketException e) {
                System.out.println("Server (" + this.getId() + "): " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    this.listener.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void closeSocket() throws IOException {
            this.listener.close();
        }
    }

    public class Handler extends Thread {
        private final Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = this.socket.getInputStream();
                ObjectInputStream input = new ObjectInputStream(is);
                OutputStream os = this.socket.getOutputStream();
                ObjectOutputStream output = new ObjectOutputStream(os);
                while (this.socket.isConnected()) {
                    Message incomingMsg = (Message) input.readObject();
                    if (incomingMsg != null) {
                        switch (incomingMsg.getMsgType()) {
                            case CONNECT: {
                                Message mReply = new Message();
                                if (users.size() == maxNumUsers) {
                                    mReply.setMsgType(MessageType.CONNECT_FAILED);
                                    mReply.setNickname("");
                                } else {
                                    User user = new User(incomingMsg.getNickname(), this.socket.getInetAddress());
                                    users.add(user);
                                    writers.add(output);
                                    controller.addUser(user);
                                    mReply.setMsgType(MessageType.USER_JOINED);
                                    mReply.setNickname(incomingMsg.getNickname());
                                    mReply.setMsgType(MessageType.CONNECT_OK);
                                    mReply.setNickname(nickname);
                                    mReply.setContent(getUserList());
                                }
                                output.writeObject(mReply);
                                break;
                            }
                            case READY: {
                                controller.updateReady(incomingMsg.getNickname(), Boolean.parseBoolean(incomingMsg.getContent()));
                                for (User user : users) {
                                    if (user.getNickname().equals(incomingMsg.getNickname())) {
                                        user.setReady(Boolean.parseBoolean(incomingMsg.getContent()));
                                        break;
                                    }
                                }
                                controller.setServerNickname();
                                controller.enableStartGame(checkCanStartGame());
                                break;
                            }
                            case START_GAME: {
                                controller.startGame();
                                break;
                            }
                            case CATEGORY: {
                                controller.addToCategory(incomingMsg.getContent());
                                if (controller.getSizeCategory() == 2) {
                                    controller.startCategory();
                                }
                                break;
                            }
                            case QUIZ: {
                                controller.setScore(incomingMsg.getContent());
                                break;
                            }
                            case DISCONNECT: {
                                controller.removeUser();
                                for (int i = 1; i < users.size(); i++) {
                                    if (users.get(i).getNickname().equals(incomingMsg.getNickname())) {
                                        users.remove(i);
                                        writers.remove(i);
                                        break;
                                    }
                                }
                                controller.enableStartGame(checkCanStartGame());
                                socket.close();
                                break;
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                if (e.getMessage().contains("Connection reset"))
                    System.out.println("Stream closed");
                else if (e.getMessage().contains("Socket closed"))
                    System.out.println("Socket closed");
                else e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Errore stream (" + this.getId() + ")");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
