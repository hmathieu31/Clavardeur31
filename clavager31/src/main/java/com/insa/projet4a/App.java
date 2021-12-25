package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

    /**
     * Username chosen by the App user and possibly changed
     * <p>
     */
    public static String pseudo;
    public static String currentDiscussionIp = "";

    /**
     * HashMap with keys of IP Addresses (formatted as string) and values of
     * Pseudonymes (formatted as Strings)
     */
    private static HashMap<String, String> userCorresp = new HashMap<String, String>();

    public static MainController controller;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(loadFXML("login_screen"), 650, 400);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> closeProgram());
        stage.setScene(scene);
        stage.setTitle("Clavager31");
        stage.show();

        // Test purpose
        addUserCorresp("localhost", "Jean");
        addUserCorresp("localhost1", "Kevin");
        addUserCorresp("localhost2", "Sebastien");
        addUserCorresp("localhost3", "Hugues");
    }

    /**
     * Gets the username of the person the App is currently communicating with
     * 
     * @return Pseudo corresponding to the IP Address of the current discussion
     */
    public static String getCurrentUserName() {
        return getUserCorresp(currentDiscussionIp);
    }

    /**
     * Gets the username corresponding to {@code ip} in the Hash Map
     * 
     * @param ip IP Address in String format
     * @return Username corresponding in string format or {@code null} if no
     *         correspondance found
     */
    public static String getUserCorresp(String ip) {
        return userCorresp.get(ip);
    }

    /**
     * Removes the username corresponding to {@code ip} in the Hash Map
     * 
     * @param ip IP Address in String format
     */
    public static void removeUserCorresp(String ip) {
        userCorresp.remove(ip);
    }

    /**
     * Adds / Updates a user in the Hash Map of address {@code ip} and username
     * {@code name}
     * 
     * @param ip   IP Address in string format
     * @param name Username in string format
     */
    public static void addUserCorresp(String ip, String name) {
        userCorresp.put(ip, name);
    }

    private void closeProgram() {
        System.out.println("GUI CLOSING");
        stage.close();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void changeSize(int width, int height) {
        stage.setHeight(height);
        stage.setWidth(width);
        stage.centerOnScreen();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /***********************************************************************/
    /*
     * BACKEND PART
     * /
     ***********************************************************************/

    private static ThreadManager threadManager;

    private static ArrayList<InetAddress> onlineUsers = new ArrayList<InetAddress>();

    /**
     * <p>
     * Called when the application is started.
     * <p>
     * If the {@code username} chosen by the user is not forbidden, broadcasts
     * {@code username} and waits for answers to fill-in {@code onlineUsers} and
     * {@code userCorresp} and check if the username was already taken
     * <p>
     * If the username was not already taken, starts a ThreadManager listening for
     * incoming communications on port 12 and completes the initialisation of
     * Clavarder31
     * <p>
     * Else, prompts the user to change its username and is called until the
     * username is valid
     * 
     * @param username Username chosen when starting the connection
     */
    public static void connect(String username) {
        threadManager = new ThreadManager(12);
        if (threadManager.initUDPHandler(username) && isPseudoValid(username)) {
            System.out.println("Pseudo valid"); // ! Remove after testing
            threadManager.startHandler();
            // System.out.println("Waiting for connexion on port 12");
        } else {
            System.err.println("Invalid username!"); // ! Remove after testing
            // TODO Prompt for a a new username when chosen invalid
        }
    }

    public static ArrayList<InetAddress> getOnlineUsers() {
        return onlineUsers;
    }

    public static void setOnlineUsers(ArrayList<InetAddress> listOnlineUsers) {
        onlineUsers = listOnlineUsers;
    }

    /**
     * Removes the user from list of online Users and the Correspondances Map
     * 
     * @param userAddress Address of the user to remove
     */
    public static void removeOnlineUser(InetAddress userAddress) {
        onlineUsers.remove(userAddress);
        removeUserCorresp(userAddress.toString());
        System.out.println("user " + userAddress + " removed"); // ? Testing purposes
    }

    /**
     * Adds the new user to the list of online users and to the Correspondances Map
     * 
     * @param newUserAddress Address of the new user
     * @param newUserPseudo  Pseudo of the new user
     */
    public static void addOnlineUsers(InetAddress newUserAddress, String newUserPseudo) {
        onlineUsers.add(newUserAddress);
        addUserCorresp(newUserAddress.toString(), newUserPseudo);
        System.out.println(onlineUsers); // ? Testing purposes
    }

    /**
     * Tries to change the username to {@code newUserName}
     * <p>
     * Fails and calls a method from the GUI if {@code newUserName} is invalid
     * 
     * @param newUserName New username, invalid if already taken by a user or if
     *                    --OFF--
     */
    public void changeUsername(String newUserName) {
        if (isPseudoValid(newUserName)) {
            threadManager.broadcastNewUsername(newUserName);
            pseudo = newUserName;
        } else {
            // TODO Notify GUI that the chosen username was invalid
        }
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

    /**
     * Checks if {@code pseudo} is already in the the table {@link userCorresp}
     * 
     * @param pseudo
     * @return True if {@code pseudo} is not already contained in the table and is
     *         not the current App pseudo
     */
    public static boolean isPseudoValid(String pseudo) {

        return !userCorresp.containsValue(pseudo) &&
                !"--OFF--".equals(pseudo) &&
                !"--INVALID--".equals(pseudo) &&
                !pseudo.equals(App.pseudo); // The user can't choose --OFF-- as
                                            // username nor his previous username nor --INVALID--
    }

    public static void main(String[] args) throws UnknownHostException {
        pseudo = args[0];
        connect(pseudo);
    }

}