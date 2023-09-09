package com.project.quizdom.game;

import com.project.quizdom.model.State;
import com.project.quizdom.model.User;
import com.project.quizdom.networking.client.Client;
import com.project.quizdom.networking.client.IClient;
import com.project.quizdom.networking.server.IServer;
import com.project.quizdom.networking.server.Server;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

public class Controller {
    @FXML
    private Text lblQuestion;
    @FXML
    private Text lblScore;
    @FXML
    private Text lblTimer;
    @FXML
    private Text lblIPAddressServer;
    @FXML
    private VBox vboxServerLobby;
    @FXML
    private VBox vboxClientLobby;
    @FXML
    private VBox vboxJoinRoom;
    @FXML
    private VBox vboxCreateRoom;
    @FXML
    private VBox vboxPlay;
    @FXML
    private VBox vboxQuiz;
    @FXML
    private VBox vboxScore;
    @FXML
    private VBox vboxCategory;
    @FXML
    private VBox vboxConnecting;
    @FXML
    private Button btnA;
    @FXML
    private Button btnB;
    @FXML
    private Button btnC;
    @FXML
    private Button btnD;
    @FXML
    private Button btnCategory1;
    @FXML
    private Button btnCategory2;
    @FXML
    private Button btnCategory3;
    @FXML
    private Button btnCategory4;
    @FXML
    private Button btnReady;
    @FXML
    private Button btnStartGame;
    @FXML
    private ListView<HBox> lstClientUsers;
    @FXML
    private ListView<HBox> lstServerUsers;
    @FXML
    private TextField txtNicknameCreate;
    @FXML
    private TextField txtNicknameJoin;
    @FXML
    private TextField txtIPAddress;
    private IServer server;
    private IClient client;
    private String currentAnswer;
    static int score = 0;
    private List<String> questions;
    static int i = 0;
    private final Timer timer = new Timer();
    private int countdown = 10;
    private int initialCountdown = 2;
    private State state;
    private final int ROOM_CAPACITY = 2;
    private int connectedUsers = 0;
    private ArrayList<Label> listNicknameClient;
    private ArrayList<Label> listReadyClient;
    private ArrayList<Label> listNicknameServer;
    private ArrayList<Label> listReadyServer;
    private ArrayList<String> randomCategory;


    public Controller() {
    }

    public void initialize() {
        this.state = State.MULTIPLAYER;
        randomCategory = new ArrayList<>();

        this.vboxPlay.setVisible(true);
        this.vboxQuiz.setVisible(false);
        this.vboxCategory.setVisible(false);
        this.vboxScore.setVisible(false);
        this.vboxServerLobby.setVisible(false);
        this.vboxClientLobby.setVisible(false);
        this.vboxJoinRoom.setVisible(false);
        this.vboxCreateRoom.setVisible(false);
        this.vboxConnecting.setVisible(false);

        this.listNicknameClient = new ArrayList<Label>();
        this.listReadyClient = new ArrayList<Label>();
        this.listNicknameServer = new ArrayList<Label>();
        this.listReadyServer = new ArrayList<Label>();

        for (int i = 0; i < ROOM_CAPACITY; i++) {

            HBox hboxClient = new HBox();
            HBox hboxServer = new HBox();
            Label nicknameClient = new Label("");
            Label nicknameServer = new Label("");
            Label readyClient = new Label("");
            Label readyServer = new Label("");

            hboxClient.setPrefSize(200, 30);
            hboxClient.setSpacing(10);
            hboxClient.setVisible(false);
            nicknameClient.setPrefWidth(200);
            nicknameClient.setTextFill(Paint.valueOf("black"));
            hboxClient.getChildren().add(nicknameClient);
            this.listNicknameClient.add(nicknameClient);

            readyClient.setPrefSize(25, 25);
            readyClient.setStyle("-fx-background-color: red");
            readyClient.setVisible(i != 0);
            hboxClient.getChildren().add(readyClient);
            this.listReadyClient.add(readyClient);

            this.lstClientUsers.getItems().add(hboxClient);

            hboxServer.setPrefSize(200, 30);
            hboxServer.setSpacing(10);
            hboxServer.setVisible(false);
            nicknameServer.setPrefWidth(200);
            nicknameServer.setTextFill(Paint.valueOf("black"));
            hboxServer.getChildren().add(nicknameServer);
            this.listNicknameServer.add(nicknameServer);

            readyServer.setPrefSize(25, 25);
            readyServer.setStyle(i == 0 ? "-fx-background-color: green" : "-fx-background-color: red");
            readyServer.setVisible(i != 0);
            hboxServer.getChildren().add(readyServer);
            this.listReadyServer.add(readyServer);

            this.lstServerUsers.getItems().add(hboxServer);

        }
        connectedUsers = 0;
    }

    private void disableAll() {
        isCorrect();
        btnA.setDisable(true);
        btnB.setDisable(true);
        btnC.setDisable(true);
        btnD.setDisable(true);
    }
    private void disableCategory(){
        btnCategory1.setDisable(true);
        btnCategory2.setDisable(true);
        btnCategory3.setDisable(true);
        btnCategory4.setDisable(true);
    }
    private void turOnCategory(){
        btnCategory1.setDisable(false);
        btnCategory2.setDisable(false);
        btnCategory3.setDisable(false);
        btnCategory4.setDisable(false);
    }

    private void turnOnAll() {
        btnA.setStyle("-fx-background-color: white");
        btnB.setStyle("-fx-background-color: white");
        btnC.setStyle("-fx-background-color: white");
        btnD.setStyle("-fx-background-color: white");
        btnA.setDisable(false);
        btnB.setDisable(false);
        btnC.setDisable(false);
        btnD.setDisable(false);
    }

    private void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (countdown > 0) {
                    Platform.runLater(() -> lblTimer.setText(String.valueOf(countdown)));
                    countdown--;
                } else {
                    if (initialCountdown > 0) {
                        Platform.runLater(() -> lblTimer.setText(String.valueOf(initialCountdown)));
                        initialCountdown--;
                        disableAll();
                        isCorrect();
                    } else {
                        Platform.runLater(() -> nextQuestion());
                    }
                }
            }
        }, 0, 1000);
    }

    private void nextQuestion() {
        turnOnAll();
        countdown = 10;
        initialCountdown = 2;
        if (questions != null && i < questions.size()) {
            lblQuestion.setText(questions.get(i++));
            btnA.setText(questions.get(i++));
            btnB.setText(questions.get(i++));
            btnC.setText(questions.get(i++));
            btnD.setText(questions.get(i++));
            currentAnswer = questions.get(i++);
        } else {
            this.vboxScore.setVisible(true);
            this.vboxQuiz.setVisible(false);
            this.lblScore.setText(String.valueOf(score));
        }
    }

    private void isCorrect() {
        if (currentAnswer.contains("A")) {
            btnA.setStyle("-fx-background-color: green");
            btnB.setStyle("-fx-background-color: red");
            btnC.setStyle("-fx-background-color: red");
            btnD.setStyle("-fx-background-color: red");
        } else if (currentAnswer.contains("B")) {
            btnB.setStyle("-fx-background-color: green");
            btnA.setStyle("-fx-background-color: red");
            btnC.setStyle("-fx-background-color: red");
            btnD.setStyle("-fx-background-color: red");
        } else if (currentAnswer.contains("C")) {
            btnC.setStyle("-fx-background-color: green");
            btnA.setStyle("-fx-background-color: red");
            btnB.setStyle("-fx-background-color: red");
            btnD.setStyle("-fx-background-color: red");
        } else if (currentAnswer.contains("D")) {
            btnD.setStyle("-fx-background-color: green");
            btnA.setStyle("-fx-background-color: red");
            btnB.setStyle("-fx-background-color: red");
            btnC.setStyle("-fx-background-color: red");
        }

    }

    private void setServerAddress() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String privateIp = socket.getLocalAddress().getHostAddress();
            this.lblIPAddressServer.setText("Server IP address: " + privateIp);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    private String drawCategory(){
        String category="";
        Random random=new Random();
        int randomValue = random.nextInt();
        if(randomCategory.size()==2){
            category=randomCategory.get(randomValue);
        }
        return category;
    }


    @FXML
    public void onPlayClicked() {
        this.vboxPlay.setVisible(false);
        this.vboxCreateRoom.setVisible(true);
        this.state = State.MP_CREATE;
    }

    @FXML
    public void onJoinClicked() {
        this.vboxPlay.setVisible(false);
        this.vboxJoinRoom.setVisible(true);
        this.state = State.MP_JOIN;
    }

    @FXML
    public void onCreateRoomClicked() throws IOException {
        this.setServerAddress();
        this.server = new Server(this, this.txtNicknameCreate.getText(), ROOM_CAPACITY);
        this.client = null;
        this.resetList();
        this.btnStartGame.setDisable(true);
        this.listNicknameServer.get(0).setText(this.txtNicknameCreate.getText());
        this.lstServerUsers.getItems().get(0).setVisible(true);
        this.connectedUsers = 1;

    }

    @FXML
    public void onJoinExistingRoomClicked() {
        this.btnReady.setText("Not Ready");
        this.btnReady.setStyle("-fx-background-color: red");
        this.showConnectingBox(true);
        this.client = new Client(this, this.txtIPAddress.getText(), 9001, this.txtNicknameJoin.getText());
        this.server = null;

    }

    @FXML
    public void onReadyClicked() throws IOException {
        if (this.btnReady.getText().equalsIgnoreCase("Ready")) {
            this.btnReady.setText("Not Ready");
            this.btnReady.setStyle("-fx-background-color: red");
            this.client.sendReady(false);
            this.updateReady(this.txtNicknameJoin.getText(), false);
        } else {
            this.btnReady.setText("Ready");
            this.btnReady.setStyle("-fx-background-color: green");
            this.client.sendReady(true);
            this.updateReady(this.txtNicknameJoin.getText(), true);
        }
    }

    @FXML
    public void onStartGameClicked() throws IOException {
        server.sendStartGame();
        this.startGame();
    }

    @FXML
    void onBtnAClicked() {
        if (currentAnswer.contains("A")) {
            score++;
        }
        disableAll();
        countdown = 0;
    }

    @FXML
    void onBtnBClicked() {
        if (currentAnswer.contains("B")) {
            score++;
        }
        disableAll();
        countdown = 0;
    }

    @FXML
    void onBtnCClicked() {
        if (currentAnswer.contains("C")) {
            score++;
        }
        disableAll();
        countdown = 0;
    }

    @FXML
    void onBtnDClicked() {
        if (currentAnswer.contains("D")) {
            score++;
        }
        disableAll();
        countdown = 0;
    }

    @FXML
    void onCategory1Clicked() throws IOException {
        addCategory("Math");
        disableCategory();
    }

    @FXML
    void onCategory2Clicked() throws IOException {
        addCategory("General");
        disableCategory();
    }

    @FXML
    void onCategory3Clicked() throws IOException {
        addCategory("Physics");
        disableCategory();
    }

    @FXML
    void onCategory4Clicked() throws IOException {
        addCategory("Biology");
        disableCategory();
    }

    public void setCategory() throws IOException {
        switch (drawCategory()){
            case "Math":{
                setQuestions("src/main/java/com/project/quizdom/game/file/matematyka.txt");
                switchToCategory();
                break;
            }
            case "General":{
                setQuestions("src/main/java/com/project/quizdom/game/file/ogolna.txt");
                switchToCategory();
                break;
            }
            case "Physics":{
                setQuestions("src/main/java/com/project/quizdom/game/file/fizyka.txt");
                switchToCategory();
                break;
            }
            case "Biology":{
                setQuestions("src/main/java/com/project/quizdom/game/file/biologia.txt");
                switchToCategory();
                break;
            }
        }
    }
    public void addCategory(String category) throws IOException {
        if (this.state == State.MP_CLIENT) {
            client.sendCategory(category);
        } else if (this.state == State.MP_SERVER) {
            randomCategory.add(category);
            server.sendCategory();
        }
    }
    public void addToCategory(String category){
        randomCategory.add(category);
    }

    void setQuestions(String category) throws FileNotFoundException {
        startTimer();
        Quiz quiz = new Quiz();
        questions = quiz.getQuestions(category);
        nextQuestion();
    }
    public void switchToCategory(){
        vboxCategory.setVisible(false);
        vboxQuiz.setVisible(true);
    }

    public void switchToMP() {
        if (this.state == State.MP_CLIENT) {
            this.vboxClientLobby.setVisible(false);
            this.vboxPlay.setVisible(true);
            this.state = State.MULTIPLAYER;
        } else if (this.state == State.MP_SERVER) {
            this.vboxServerLobby.setVisible(false);
            this.vboxPlay.setVisible(true);
            this.state = State.MULTIPLAYER;
        }
    }

    public void switchToServerRoom() {
        this.vboxCreateRoom.setVisible(false);
        this.vboxServerLobby.setVisible(true);
        this.state = State.MP_SERVER;
    }

    public void switchToClientRoom() {
        this.vboxJoinRoom.setVisible(false);
        this.vboxClientLobby.setVisible(true);
        this.state = State.MP_CLIENT;
    }

    public void updateReady(String nickname, boolean ready) {
        if (this.state == State.MP_CLIENT) {
            for (int i = 0; i < this.lstClientUsers.getItems().size(); i++) {
                if (nickname.equals(this.listNicknameClient.get(i).getText())) {
                    this.listReadyClient.get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
                    break;
                }
            }
        } else if (this.state == State.MP_SERVER) {
            for (int i = 0; i < this.lstServerUsers.getItems().size(); i++) {
                if (nickname.equals(this.listNicknameServer.get(i).getText())) {
                    this.listReadyServer.get(i).setStyle(ready ? "-fx-background-color: lime" : "-fx-background-color: red");
                    break;
                }
            }
        }
    }

    public void startGame() throws IOException {
        if (this.state == State.MP_CLIENT) {
            vboxClientLobby.setVisible(false);
            vboxCategory.setVisible(true);
        } else if (this.state == State.MP_SERVER) {
            vboxServerLobby.setVisible(false);
            vboxCategory.setVisible(true);
        }
    }
    public void resetList() {
        if (this.state == State.MP_CLIENT) {
            Platform.runLater(() -> {
                for (int i = 0; i < ROOM_CAPACITY; i++) {
                    this.lstClientUsers.getItems().get(i).setVisible(false);
                    this.listNicknameClient.get(i).setText("");
                    this.listReadyClient.get(i).setStyle("-fx-background-color: red");
                }
            });
        } else if (this.state == State.MP_SERVER) {
            for (int i = 0; i < ROOM_CAPACITY; i++) {
                this.lstServerUsers.getItems().get(i).setVisible(false);
                this.listNicknameServer.get(i).setText("");
                this.listReadyServer.get(i).setStyle("-fx-background-color: red");
            }
        }
    }

    public void addUser(User user) {
        Platform.runLater(() ->
        {
            if (this.state == State.MP_CLIENT) {
                this.listNicknameClient.get(this.connectedUsers).setText(user.getNickname());
                this.lstClientUsers.getItems().get(this.connectedUsers).setVisible(true);
                this.connectedUsers++;
            } else if (this.state == State.MP_SERVER) {
                this.listNicknameServer.get(this.connectedUsers).setText(user.getNickname());
                this.lstServerUsers.getItems().get(this.connectedUsers).setVisible(true);
                this.connectedUsers++;
                this.btnStartGame.setDisable(true);
            }
        });
    }

    public void updateUserList(List<User> users) {
        Platform.runLater(() -> {
            if (this.state == State.MP_CLIENT) {
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    this.listNicknameClient.get(i).setText(user.getNickname());
                    this.lstClientUsers.getItems().get(i).setVisible(true);
                    this.listReadyClient.get(i).setStyle(user.isReady() ? "-fx-background-color: green" : "-fx-background-color: red");
                }
                this.connectedUsers = users.size();
            }
        });
    }

    public void enableStartGame(boolean value) {
        this.btnStartGame.setDisable(!value);
    }

    public void closeConnection() throws IOException {
        if (this.state == State.MP_CLIENT) {
            this.client.sendClose();
            this.client = null;
        } else if (this.state == State.MP_SERVER) {
            this.server.sendClose();
            this.server = null;
        }
    }

    public void removeUser(String nickname) {
        Platform.runLater(() -> {
            boolean found = false;
            if (this.state == State.MP_CLIENT) {
                this.lstClientUsers.getItems().get(this.connectedUsers - 1).setVisible(false);
                this.listNicknameClient.get(this.connectedUsers - 1).setText("");
                this.listReadyClient.get(this.connectedUsers - 1).setStyle("-fx-background-color: red");
                this.connectedUsers--;
            } else if (this.state == State.MP_SERVER) {
                this.lstServerUsers.getItems().get(this.connectedUsers - 1).setVisible(false);
                this.listNicknameServer.get(this.connectedUsers - 1).setText("");
                this.listReadyServer.get(this.connectedUsers - 1).setStyle("-fx-background-color: red");
                this.connectedUsers--;
                this.btnStartGame.setDisable(!this.server.checkCanStartGame());
            }
        });
    }

    public void showConnectingBox(boolean value) {
        this.vboxConnecting.setVisible(value);
    }

    public boolean isRoomOpen() {
        return true;
    }
}
