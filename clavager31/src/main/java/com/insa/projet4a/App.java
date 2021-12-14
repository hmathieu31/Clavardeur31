package com.insa.projet4a;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Stage stage;

    public static String pseudo;
    public static String currentDiscussionIp = "";

    private static HashMap<String,String> userCorresp = new HashMap<String,String>();

    public static MainController controller;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(loadFXML("login_screen"), 650, 400);
        stage.setResizable(false);
        stage.setOnCloseRequest(e->closeProgram());
        stage.setScene(scene);
        stage.setTitle("Clavager31");
        stage.show();

        // Test purpose
        addUserCorresp("localhost",  "Jean");
        addUserCorresp("localhost1", "Kevin");
        addUserCorresp("localhost2", "Sebastien");
        addUserCorresp("localhost3", "Hugues");
    }

    public static String getCurrentUserName(){
        return getUserCorresp(currentDiscussionIp);
    }

    public static String getUserCorresp(String ip) {
        return userCorresp.get(ip) ;
    }

    public static void removeUserCorresp(String ip) {
        userCorresp.remove(ip);
    }

    public static void addUserCorresp(String ip, String name) {
        userCorresp.put(ip, name);
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
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}