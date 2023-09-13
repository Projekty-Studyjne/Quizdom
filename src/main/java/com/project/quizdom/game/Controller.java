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

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

public class Controller {
    @FXML
    private Text lblQuestion;
    @FXML
    private Text lblScore;
    @FXML
    private Text lblTimer;
    @FXML
    private Text lblErrorNicknameClient;
    @FXML
    private Text lblErrorIP;
    @FXML
    private Text lblErrorNicknameServer;
    @FXML
    private Text lblIPAddressServer;
    @FXML
    private Text lblResult;
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
    private VBox vboxBack;
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
    private Button btnCategoryMath;
    @FXML
    private Button btnCategoryGeneralKnowledge;
    @FXML
    private Button btnCategoryPhysics;
    @FXML
    private Button btnCategoryBiology;
    @FXML
    private Button btnCategoryHistory;
    @FXML
    private Button btnCategoryGeography;
    @FXML
    private Button btnCategoryReligion;
    @FXML
    private Button btnCategoryMusic;
    @FXML
    private Button btnReady;
    @FXML
    private Button btnStartGame;
    @FXML
    private Button btnJoinRoom;
    @FXML
    private Button btnCreateRoom;
    @FXML
    private Button btnPlayAgain;
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
    static int clientScore = 0;
    static int serverScore = 0;
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
    private static List<Integer> randomQuestions;
    private static int j = 1;
    private static int ready = 0;
    private String clientNickname;
    private String serverNickname;
    private static final Pattern PATTERN_NICKNAME = Pattern.compile("^[a-zA-Z0-9]{3,15}$");
    private static final Pattern PATTERN_IP = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public Controller() {
    }

    public void initialize() {
        this.state = State.MULTIPLAYER;
        randomCategory = new ArrayList<>();

        this.vboxPlay.setVisible(true);
        this.vboxBack.setVisible(true);
        this.vboxQuiz.setVisible(false);
        this.vboxCategory.setVisible(false);
        this.vboxScore.setVisible(false);
        this.vboxServerLobby.setVisible(false);
        this.vboxClientLobby.setVisible(false);
        this.vboxJoinRoom.setVisible(false);
        this.vboxCreateRoom.setVisible(false);
        this.vboxConnecting.setVisible(false);
        this.lblErrorIP.setVisible(false);
        this.lblErrorNicknameClient.setVisible(false);
        this.lblErrorNicknameServer.setVisible(false);


        this.listNicknameClient = new ArrayList<>();
        this.listReadyClient = new ArrayList<>();
        this.listNicknameServer = new ArrayList<>();
        this.listReadyServer = new ArrayList<>();

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

    private boolean checkNickname(String text) {
        return PATTERN_NICKNAME.matcher(text).matches();
    }

    private boolean checkIP(String text) {
        return PATTERN_IP.matcher(text).matches();
    }

    private void disableAll() {
        isCorrect();
        btnA.setDisable(true);
        btnB.setDisable(true);
        btnC.setDisable(true);
        btnD.setDisable(true);
    }

    private void disableCategory() {
        btnCategoryMath.setDisable(true);
        btnCategoryGeneralKnowledge.setDisable(true);
        btnCategoryPhysics.setDisable(true);
        btnCategoryBiology.setDisable(true);
        btnCategoryHistory.setDisable(true);
        btnCategoryGeography.setDisable(true);
        btnCategoryReligion.setDisable(true);
        btnCategoryMusic.setDisable(true);
    }

    private void turOnCategory() {
        btnCategoryMath.setDisable(false);
        btnCategoryGeneralKnowledge.setDisable(false);
        btnCategoryPhysics.setDisable(false);
        btnCategoryBiology.setDisable(false);
        btnCategoryHistory.setDisable(false);
        btnCategoryGeography.setDisable(false);
        btnCategoryReligion.setDisable(false);
        btnCategoryMusic.setDisable(false);
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
                    Platform.runLater(() -> lblTimer.setText("Timer: " + countdown));
                    countdown--;
                } else {
                    if (initialCountdown > 0) {
                        Platform.runLater(() -> lblTimer.setText("Next question: " + initialCountdown));
                        initialCountdown--;
                        disableAll();
                        isCorrect();
                    } else {
                        Platform.runLater(() -> {
                            initialCountdown = 2;
                            try {
                                nextQuestion();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            }
        }, 0, 1000);
    }

    private void nextQuestion() throws IOException {
        turOnCategory();
        turnOnAll();
        countdown = 10;
        if (questions != null && i < questions.size()) {
            Platform.runLater(() -> {
                lblQuestion.setText((j++) + ". " + questions.get(i++));
                btnA.setText(questions.get(i++));
                btnB.setText(questions.get(i++));
                btnC.setText(questions.get(i++));
                btnD.setText(questions.get(i++));
                currentAnswer = questions.get(i++);
            });
        } else {
            setEndingScore();
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

    private String drawCategory() {
        String category = "";
        Random random = new Random();
        int randomValue = random.nextInt(2);
        if (randomCategory.size() == 2) {
            category = randomCategory.get(randomValue);
        }
        return category;
    }

    private static List<Integer> drawQuestions() {
        List<Integer> draw = new ArrayList<>();
        for (int i = 0; i <= 129; i += 6) {
            draw.add(i);
        }
        Collections.shuffle(draw);
        return draw.subList(0, 5);
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
    void onBtnAClicked() throws IOException {
        checkAnswer("A");
        disableAll();
    }

    @FXML
    void onBtnBClicked() throws IOException {
        checkAnswer("B");
        disableAll();
    }

    @FXML
    void onBtnCClicked() throws IOException {
        checkAnswer("C");
        disableAll();
    }

    @FXML
    void onBtnDClicked() throws IOException {
        checkAnswer("D");
        disableAll();
    }

    @FXML
    void onCategoryMathClicked() throws IOException {
        addCategory("Math");
        disableCategory();
    }

    @FXML
    void onCategoryGeneralKnowledgeClicked() throws IOException {
        addCategory("GeneralKnowledge");
        disableCategory();
    }

    @FXML
    void onCategoryPhysicsClicked() throws IOException {
        addCategory("Physics");
        disableCategory();
    }

    @FXML
    void onCategoryBiologyClicked() throws IOException {
        addCategory("Biology");
        disableCategory();
    }

    @FXML
    void onCategoryHistoryClicked() throws IOException {
        addCategory("History");
        disableCategory();
    }

    @FXML
    void onCategoryGeographyClicked() throws IOException {
        addCategory("Geography");
        disableCategory();
    }

    @FXML
    void onCategoryReligionClicked() throws IOException {
        addCategory("Religion");
        disableCategory();
    }

    @FXML
    void onCategoryMusicClicked() throws IOException {
        addCategory("Music");
        disableCategory();
    }

    @FXML
    void onBackClicked() throws IOException {
        switch (this.state) {
            case MP_CREATE: {
                this.vboxCreateRoom.setVisible(false);
                this.vboxPlay.setVisible(true);
                this.state = State.MULTIPLAYER;
                break;
            }
            case MP_JOIN: {
                this.vboxJoinRoom.setVisible(false);
                this.vboxPlay.setVisible(true);
                this.state = State.MULTIPLAYER;
                break;
            }
            case MP_SERVER: {
                this.closeConnection();
                this.vboxServerLobby.setVisible(false);
                this.vboxPlay.setVisible(true);
                this.state = State.MULTIPLAYER;
                break;
            }
            case MP_CLIENT: {
                this.closeConnection();
                this.vboxClientLobby.setVisible(false);
                this.vboxPlay.setVisible(true);
                this.state = State.MULTIPLAYER;
                break;
            }
        }
    }

    @FXML
    public void validateNicknameAddressClient() {
        if (this.checkNickname(this.txtNicknameJoin.getText()) && (this.checkIP(this.txtIPAddress.getText()) || this.txtIPAddress.getText().isEmpty())) {
            this.btnJoinRoom.setDisable(false);
            this.lblErrorNicknameClient.setVisible(false);
            this.lblErrorIP.setVisible(false);
            this.txtNicknameJoin.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
            this.txtIPAddress.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
        } else if (this.checkNickname(this.txtNicknameJoin.getText()) && !(checkIP(this.txtIPAddress.getText()) || this.txtIPAddress.getText().isEmpty())) {
            this.btnJoinRoom.setDisable(true);
            this.lblErrorNicknameClient.setVisible(false);
            this.lblErrorIP.setVisible(true);
            this.txtNicknameJoin.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
            this.txtIPAddress.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
        } else if (!this.checkNickname(this.txtNicknameJoin.getText()) && (checkIP(this.txtIPAddress.getText()) || this.txtIPAddress.getText().isEmpty())) {
            this.btnJoinRoom.setDisable(true);
            this.lblErrorNicknameClient.setVisible(true);
            this.lblErrorIP.setVisible(false);
            this.txtNicknameJoin.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            this.txtIPAddress.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
        } else {
            this.btnJoinRoom.setDisable(true);
            this.lblErrorNicknameClient.setVisible(true);
            this.lblErrorIP.setVisible(true);
            this.txtNicknameJoin.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
            this.txtIPAddress.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
        }
    }

    @FXML
    public void validateNicknameServer() {
        if (this.checkNickname(this.txtNicknameCreate.getText())) {
            this.btnCreateRoom.setDisable(false);
            this.lblErrorNicknameServer.setVisible(false);
            this.txtNicknameCreate.setStyle("-fx-border-width: 0px; -fx-focus-color: #039ED3;");
        } else {
            this.btnCreateRoom.setDisable(true);
            this.lblErrorNicknameServer.setVisible(true);
            this.txtNicknameCreate.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
        }
    }

    @FXML
    public void onPlayAgainClicked() throws IOException {
        btnPlayAgain.setDisable(true);
        playAgain();
    }

    public void playAgain() throws IOException {
        if (this.state == State.MP_CLIENT) {
            client.sendPlayAgain();
        } else if (this.state == State.MP_SERVER) {
            playAgainReady();
            if (getReady() == 2) {
                server.sendStartGame();
                startGame();
            }
        }
    }

    public void playAgainReady() {
        ready++;
    }

    public int getReady() {
        return ready;
    }

    public void checkAnswer(String answer) throws IOException {
        if (this.state == State.MP_CLIENT) {
            if (currentAnswer.contains(answer)) {
                client.sendCorrectAnswer(clientScore++);
            } else {
                client.sendWrongAnswer(clientScore);
            }
        } else if (this.state == State.MP_SERVER) {
            if (currentAnswer.contains(answer)) {
                serverScore++;
            }
            server.sendScore(serverScore);
        }
    }

    public void setScore(String score) {
        if (this.state == State.MP_CLIENT) {
            serverScore = Integer.parseInt(score);
        } else if (this.state == State.MP_SERVER) {
            clientScore = Integer.parseInt(score);
        }
    }

    public void setEndingScore() throws IOException {
        if (this.state == State.MP_CLIENT) {
            client.sendEnding();
        } else if (this.state == State.MP_SERVER) {
            server.sendEnding();
            switchToEnding();
        }
    }

    public void switchToEnding() {
        this.vboxScore.setVisible(true);
        this.vboxQuiz.setVisible(false);
        if (this.state == State.MP_CLIENT) {
            this.lblScore.setText(clientNickname + ": " + clientScore + "---" + serverScore + ": " + serverNickname);
            if (clientScore > serverScore) {
                lblResult.setText("YOU WIN!!!");
            } else if (clientScore < serverScore) {
                lblResult.setText("SKILL ISSUE :(");
            } else {
                lblResult.setText("DRAW");
            }
        } else if (this.state == State.MP_SERVER) {
            this.lblScore.setText(serverNickname + ": " + serverScore + "---" + clientScore + ": " + clientScore);
            if (clientScore > serverScore) {
                lblResult.setText("SKILL ISSUE :(");
            } else if (clientScore < serverScore) {
                lblResult.setText("YOU WIN!!!");
            } else {
                lblResult.setText("DRAW");
            }
        }
    }

    public void setClientNickname(String nickname) {
        clientNickname = nickname;
    }

    public void setServerNickname(String nickname) {
        serverNickname = nickname;
    }

    public void setCategory(String category) throws IOException {
        switch (category) {
            case "Math": {
                setQuestions("src/main/java/com/project/quizdom/game/file/Math.txt");
                break;
            }
            case "GeneralKnowledge": {
                setQuestions("src/main/java/com/project/quizdom/game/file/GeneralKnowledge.txt");
                break;
            }
            case "Physics": {
                setQuestions("src/main/java/com/project/quizdom/game/file/Physics.txt");
                break;
            }
            case "Biology": {
                setQuestions("src/main/java/com/project/quizdom/game/file/Biology.txt");
                break;
            }
            case "History": {
                setQuestions("src/main/java/com/project/quizdom/game/file/History.txt");
                break;
            }
            case "Geography": {
                setQuestions("src/main/java/com/project/quizdom/game/file/Geography.txt");
                break;
            }
            case "Religion": {
                setQuestions("src/main/java/com/project/quizdom/game/file/Religion.txt");
                break;
            }
            case "Music": {
                setQuestions("src/main/java/com/project/quizdom/game/file/Music.txt");
                break;
            }
        }
    }

    public void setDrawQuestions(Integer questions) {
        randomQuestions.add(questions);
    }


    public void startCategory() throws IOException {
        String category = drawCategory();
        randomQuestions = drawQuestions();
        setCategory(category);
        server.sendCategory(category);
        for (Integer randomQuestion : randomQuestions) {
            server.sendQuestion(String.valueOf(randomQuestion));
        }
        switchToQuiz();
    }

    public void addCategory(String category) throws IOException {
        if (this.state == State.MP_CLIENT) {
            client.sendCategory(category);
        } else if (this.state == State.MP_SERVER) {
            addToCategory(category);
            if (getSizeCategory() == 2) {
                startCategory();
            }
        }
    }

    public void addToCategory(String category) {
        randomCategory.add(category);
    }

    void setQuestions(String category) throws IOException {
        startTimer();
        Quiz quiz = new Quiz();
        questions = new ArrayList<>();
        List<String> tempQuestions = quiz.getQuestions(category);
        for (Integer randomQuestion : randomQuestions) {
            for (int j = randomQuestion; j < randomQuestion + 5; j++) {
                questions.add(tempQuestions.get(j));
            }
        }
        nextQuestion();
    }

    public void switchToQuiz() {
        vboxCategory.setVisible(false);
        vboxQuiz.setVisible(true);
    }

    public int getSizeCategory() {
        return randomCategory.size();
    }

    public void switchToMP() {
        if (this.state == State.MP_CLIENT) {
            this.vboxClientLobby.setVisible(false);
            this.vboxQuiz.setVisible(false);
            this.vboxCategory.setVisible(false);
            this.vboxScore.setVisible(false);
            this.vboxPlay.setVisible(true);
            this.state = State.MULTIPLAYER;
        } else if (this.state == State.MP_SERVER) {
            this.vboxServerLobby.setVisible(false);
            this.vboxQuiz.setVisible(false);
            this.vboxCategory.setVisible(false);
            this.vboxScore.setVisible(false);
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
            vboxBack.setVisible(false);
            vboxCategory.setVisible(true);
        } else if (this.state == State.MP_SERVER) {
            vboxServerLobby.setVisible(false);
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
}
