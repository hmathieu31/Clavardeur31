package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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

    private static final int TCP_PORT = 60000;

    private static String currentDiscussionIp = "";

    private static boolean isMainControllerInit = false;

    private static MainController controller;

    private static Scene scene;

    private static Stage stage;

    private static boolean hasConnected = false;

    private static ThreadManager threadManager = new ThreadManager(TCP_PORT);

    private static ArrayList<InetAddress> onlineUsers = new ArrayList<>();

    private static String pseudo;

    private static HashMap<String, String> userCorresp = new HashMap<>();

    private static final Logger LOGGER = Logger.getLogger("clavarder.app");

    /**
     *
     * @return The IP address of the user currently in discussion with formatted as
     *         a String of 5 bytes
     */
    public static String getCurrentDiscussionIp() {
        return currentDiscussionIp;
    }

    /**
     * currentDiscussionIP is the IP address of the user currently with which the
     * conversation screen is being opened.
     * 
     * @param currentDiscussionIp must be formatted as a 5 bytes String
     *                            (InetAddress.getHost() method)
     */
    public static void setCurrentDiscussionIp(String currentDiscussionIp) {
        App.currentDiscussionIp = currentDiscussionIp;
    }

    /**
     * Called when the MainController is initialized to allow the App to process.
     * 
     * @param isMainControllerInit
     */
    public static void setMainControllerInit(boolean isMainControllerInit) {
        App.isMainControllerInit = isMainControllerInit;
    }

    public static void setController(MainController controller) {
        App.controller = controller;
    }

    /**
     * Gets the current JavaFX stage
     * 
     * @return
     */
    public static Stage getStage() {
        return stage;
    }

    /**
     * Sets a JavaFX stage
     * 
     * @param stage
     */
    public static void setStage(Stage stage) {
        App.stage = stage;
    }

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

    /**
     * 
     * @return the userCorresp of IPs into usernames of all connected user.
     */
    public static Map<String, String> getUserCorresp() {
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
     * online users with different access methods is relevant
     * 
     * @param ip   IP Address in string format --> if the address was already in,
     *             changes the corresponding username
     * @param name Username in string format
     */
    public static void addUserCorresp(String ip, String name) {
        userCorresp.put(ip, name);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(loadFXML("login_screen"), 650, 400);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Clavardeur31");
        stage.show();

    }

    /**
     * Called when the GUI is closed.
     * <p>
     * Close the current {@code stage}.
     */
    @Override
    public void stop() {
        LOGGER.info("GUI closing");
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

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /***********************************************************************/
    /*
     * BACKEND METHODS
     * /
     ***********************************************************************/

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
                for (InetAddress inetAddress : onlineUsers) {
                    newDiscussion(inetAddress);
                }
            }
        } else {
            if (isPseudoValid(username)) {
                pseudoValidity = true;
                try {
                    threadManager.broadcastChangeUsername(username);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pseudoValidity;
    }

    public static List<InetAddress> getOnlineUsers() {
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
            // newDiscussion(newUserAddress);
        } else {
            if (isMainControllerInit) { // ! Possibly useless condition
                Platform.runLater(() -> controller.updateConnected(newUserAddress.getHostAddress()));
            }
        }
        LOGGER.info(() -> "IP: " + newUserAddress + " - name:" + newUserPseudo);
    }

    /**
     * Creates a ClientThread to send messages to {@code receivAddress}
     * 
     * @param receivAddress Address of the destinary
     */
    public static void newDiscussion(InetAddress receivAddress) {
        try {
            threadManager.createClientThread(TCP_PORT, receivAddress);
        } catch (IOException e) {
            LOGGER.severe(() -> "Failed to establish connexion with target " + receivAddress);
            e.printStackTrace();
        }
    }

    /**
     * Display a received message
     * 
     * @param msg     Message received
     * @param address Address of the sender
     */
    public static void displayMsg(String msg, InetAddress address) {
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
        LOGGER.info(() -> "Connexion closed by local initiative with " + receivAddress);
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
            try {
                threadManager.broadcastDisconnection();
                threadManager.stopUDPHandler();
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

    public static void main(String[] args) throws InterruptedException, SecurityException, IOException {
        FileHandler fileHandler = new FileHandler("logs.log");
        LOGGER.addHandler(fileHandler);
        fileHandler.setFormatter(new SimpleFormatter());
        
        launch();
        
        LOGGER.info("Exited");

    }
}