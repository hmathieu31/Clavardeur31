package com.insa.projet4a;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class GUI extends Application {

    private static Scene scene;
    private static Stage stage;

    public static String pseudo;
    public static int currentDiscussionIndex = -1;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(loadFXML("login_screen"), 650, 400);
        stage.setOnCloseRequest(e->closeProgram());
        stage.setScene(scene);
        stage.setTitle("Clavager31");
        stage.show();
    }

    private void closeProgram(){
        System.out.println("GUI CLOSING");
        stage.close();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void changeSize(int width, int height){
        stage.setHeight(height);
        stage.setWidth(width);
        stage.centerOnScreen();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GUI.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}