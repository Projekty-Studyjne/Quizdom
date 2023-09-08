module com.project.quizdom {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.project.quizdom to javafx.fxml;
    opens com.project.quizdom.game to javafx.fxml;
    exports com.project.quizdom;
    exports com.project.quizdom.game;
}