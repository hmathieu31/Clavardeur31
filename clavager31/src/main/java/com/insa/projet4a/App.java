package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

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

    private static ThreadManager threadManager;

    private static ArrayList<InetAddress> onlineUsers = new ArrayList<InetAddress>();

    public static String pseudo;

    /**
     * <p>
     * Function called when the application is started.
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
        if (threadManager.initUDPHandler("toto")) {
            System.out.println("Pseudo valid");
        } else {
            System.out.println("Pseudo invalid");
        }
    }


    public static ArrayList<InetAddress> getOnlineUsers() {
        return onlineUsers;
    }

    public static void setOnlineUsers(ArrayList<InetAddress> listOnlineUsers) {
        onlineUsers = listOnlineUsers;
    }

    /**
     * Removes the user from list of online Users
     * 
     * @param userAddress Address of the user to remove
     */
    public static void removeOnlineUser(InetAddress userAddress) {
        onlineUsers.remove(userAddress);
        System.out.println("user " + userAddress + " removed");
    }

    /**
     * Adds the new user to the list of online users
     * 
     * @param newUserAddress Address of the new user
     */
    public static void addOnlineUsers(InetAddress newUserAddress, String newUserPseudo) {
        App.onlineUsers.add(newUserAddress);
        System.out.println(onlineUsers);
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

    /**
     * Ends all the Client and Server Threads and Broadcast and exit message
     * <p>
     * Called when the application is closed
     */
    public void disconnect() {
        for (InetAddress inetAddress : onlineUsers) {
            endDiscussion(inetAddress);
        }
        threadManager.broadcastDisconnection();
        threadManager.stopUDPHandler();
    }

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) throws UnknownHostException {
        connect();


    }

}