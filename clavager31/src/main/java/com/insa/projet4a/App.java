package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

    /***********************************************************************/
    /* BACKEND PART
    /***********************************************************************/

    private static ThreadManager threadManager;

    /**
     * <p>
     * Function called when the application is started.
     * </p>
     * <p>
     * Starts a ThreadManager listening for incoming communications
     * on port 12.
     * </p>
     */
    public static void connect() {
        threadManager = new ThreadManager(12);
        threadManager.startServer();
        System.out.println("Waiting for connexion on port 12");
        System.out.println();
    }

    /**
     * Creates a ClientThread to send messages to {@code receivAddress}
     * 
     * @param receivAddress Address of the destinary
     * @return {@code True} if the establishment of the discussion was successful.
     *         False
     *         otherwise
     */
    public static boolean newDiscussion(InetAddress receivAddress) {
        try {
            threadManager.createClientThread(12, receivAddress);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to establish connexion with target");
            return false;
        }
    }

    /**
     * Display a received message
     * TODO #3 Change to call the GUI
     * 
     * @param msg     Message received
     * @param address Address of the sender
     */
    public static void displayMsg(String msg, InetAddress address) {
        System.out.println("Msg received from " + address + " --- " + msg);
    }

    /**
     * Called by the Thread Manager to notify the Application that {@code address}
     * closed the connection
     * <p>
     * TODO #2 Change to call the GUI
     * 
     * @param address address of the remote client
     */
    public static void notifyConnectionClosed(InetAddress address) {
        System.out.println("Connection closed by remote initiative with " + address);
        // TODO #1 Integration with GUI - Remove the user from ongoing connections list
    }

    /**
     * Transmits a message to another user
     * 
     * @param msg           Message transmitted
     * @param receivAddress Address of the user
     */
    public static void transmitMessage(String msg, InetAddress receivAddress) {
        threadManager.transmitMessage(msg, receivAddress);
    }

    /**
     * <p>
     * Ends the discussion with a specified address{@code receivAddress}
     * </p>
     * 
     * @param receivAddress
     */
    public static void endDiscussion(InetAddress receivAddress) {
        ThreadManager.closeConnectionThreads(receivAddress);
        System.out.println("Connexion closed by local initiative with " + receivAddress);
    }

    public static void main(String[] args) {
        launch();
    }

}