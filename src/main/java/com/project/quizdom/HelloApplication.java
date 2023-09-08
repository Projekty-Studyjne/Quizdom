package com.project.quizdom;

import com.project.quizdom.game.Controller;
import com.project.quizdom.game.Quiz;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class HelloApplication extends Application {
    private Controller controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/project/quizdom/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        this.controller = (Controller) fxmlLoader.getController();
    }
    @Override
    public void stop() throws Exception {
        this.controller.closeConnection();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        launch();
    }
}