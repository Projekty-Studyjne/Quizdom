package com.project.quizdom.networking.server;

import com.project.quizdom.game.Controller;
import com.project.quizdom.model.Message;
import com.project.quizdom.model.MessageType;
import com.project.quizdom.model.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements IServer {
    private static final int PORT = 9001;
    private int maxNumUsers = 2;
    private final Controller controller;
    private final String nickname;
    private final ArrayList<User> users;
    private ServerListener serverListener;
    private final ArrayList<ObjectOutputStream> writers;

    public Server(Controller controller, String nickname, int usersRequired) throws IOException {
        this.controller = controller;
        this.nickname = nickname;
        this.maxNumUsers = usersRequired;
        this.users = new ArrayList<User>();
        User user = new User(nickname);
        user.setReady(true);
        this.users.add(user);
        this.writers = new ArrayList<ObjectOutputStream>();
        this.writers.add(null);
        this.serverListener = new ServerListener(PORT);
        this.serverListener.start();
        this.controller.switchToServerRoom();

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
        serverListener = new ServerListener(9001);
        serverListener.closeSocket();
    }

    private String getUserList() {
        StringBuilder list = new StringBuilder("");
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
                                } else if (!controller.isRoomOpen()) {
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
                                controller.enableStartGame(checkCanStartGame());
                                break;
                            }
                            case DISCONNECT: {
                                controller.removeUser(incomingMsg.getNickname());
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
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
