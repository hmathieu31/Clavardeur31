package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
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
     */
    private static String pseudo;

    /**
     * Getter for App pseudo
     * 
     * @return Current username
     */
    public static String getPseudo() {
        return pseudo;
    }

    /**
     * Setter for App pseudo
     * 
     * @param pseudo Chosen username
     */
    public static void setPseudo(String pseudo) {
        App.pseudo = pseudo;
    }

    public static String currentDiscussionIp = "";

    /**
     * HashMap with keys of IP Addresses (formatted as string) and values of
     * Pseudonymes (formatted as Strings)
     */
    private static HashMap<String, String> userCorresp = new HashMap<String, String>();

    public static MainController controller;
    private static boolean hasConnected = false;

    public static boolean isMainControllerInit = false;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(loadFXML("login_screen"), 650, 400);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> closeProgram());
        stage.setScene(scene);
        stage.setTitle("Clavager31");
        stage.show();

    }

    public static HashMap<String, String> getUserCorresp() {
        return userCorresp;
    }

    /**
     * Gets the username corresponding to {@code ip} in the Hash Map
     * 
     * @param ip IP Address in String format
     * @return Username corresponding in string format or {@code null} if no
     *         correspondance found
     */
    public static String getPseudoFromIP(String ip) {
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
     * TODO [CLAV-38]Should check whether keeping an arraylist and a HashMap for
     * online users with different access methods is relevant
     * 
     * @param ip   IP Address in string format --> if the address was already in,
     *             changes the corresponding username
     * @param name Username in string format
     */
    public static void addUserCorresp(String ip, String name) {
        userCorresp.put(ip, name);
        System.out.println("user " + ip + " - " + name);
    }

    /**
     * Called when the GUI is closed.
     * <p>
     * Close the current {@code stage}.
     */
    private void closeProgram() {
        System.out.println("GUI CLOSING");
        stage.close();
        disconnect();
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

    private static ThreadManager threadManager = new ThreadManager(60000);

    private static ArrayList<InetAddress> onlineUsers = new ArrayList<InetAddress>();

    /**
     * <p>
     * Called by the {@code Login Screen} when the User enters his username to check
     * if the pseudo is valid.
     * <p>
     * If the {@code username} chosen by the user is not a forbidden keyword,
     * broadcasts
     * {@code username} and waits for answers to fill-in {@code onlineUsers} and
     * {@code userCorresp} and check if the username was already taken
     * <p>
     * --> If the username was not already taken, starts the ThreadManager listening
     * for
     * incoming communications on port 60000 and completes the initialisation of
     * Clavarder31
     * <p>
     * --> Else returns false and is called again by the {@code Login Screen} until
     * a
     * valid username is provided
     * 
     * @param username Username chosen when starting the connection
     * @return True if the chosen username is valid
     */
    public static boolean isInitPseudoValid(String username) {
        boolean pseudoValidity = false;
        if (!hasConnected) {
            if (threadManager.initUDPHandler(username) && isPseudoValid(username)) {
                pseudoValidity = true;
                threadManager.startHandler();
                threadManager.startUDPListener();
                hasConnected = true;
            }
        } else {
            if (isPseudoValid(username)) {
                pseudoValidity = true;
                threadManager.broadcastNewUsername(username);
            }
        }
        return pseudoValidity;
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

        Platform.runLater(() -> {
            try {
                controller.removeConnected(userAddress.getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        endDiscussion(userAddress);

        removeUserCorresp(userAddress.getHostAddress());
        System.out.println("user " + userAddress + " removed"); // ! Testing purposes
    }

    /**
     * Adds the new user to the list of online users and to the Correspondances Map
     * 
     * @param newUserAddress Address of the new user
     * @param newUserPseudo  Pseudo of the new user
     * @throws IOException
     */
    public static void addOnlineUsers(InetAddress newUserAddress, String newUserPseudo) throws IOException {
        addUserCorresp(newUserAddress.getHostAddress(), newUserPseudo);
        if (!onlineUsers.contains(newUserAddress)) {
            onlineUsers.add(newUserAddress);
            if (isMainControllerInit) {
                Platform.runLater(() -> {
                    try {
                        controller.addConnected(newUserAddress.getHostAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            newDiscussion(newUserAddress);
        } else {
            if (isMainControllerInit) { // ! Possibly useless conditional
                Platform.runLater(() -> {
                    controller.updateConnected(newUserAddress.getHostAddress());
                });
            }
        }
        System.out.println("IP: " + newUserAddress + " - name:" + newUserPseudo); // ! Testing purposes
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
            threadManager.createClientThread(60000, receivAddress);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to establish connexion with target " + receivAddress);
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
        Platform.runLater(() -> {
            try {
                controller.receiveMessage(address.getHostAddress(), msg);
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        });
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
        if (hasConnected) {
            threadManager.broadcastDisconnection();
            threadManager.stopUDPHandler();
            try {
                threadManager.stopHandler();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                !"".equals(pseudo);
    }

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        launch();

        System.out.println("Exited");
    }
}