package sec.assignment.app.controller;

import javafx.application.Application;
import javafx.stage.Stage;
import sec.assignment.app.view.FileCompareApp;

public class Launcher extends Application {
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (RuntimeException e){
            System.out.println("Failed running the JavaFX runtime: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) {
        new FileCompareApp().start(stage);
    }
}
