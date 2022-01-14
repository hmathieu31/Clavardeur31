package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.util.Pair;

/**
 * The {@code ThreadManager} class defines a Thread Manager
 * <p>
 * A Thread Manager creates a {@link UDPHandler}, listens for incoming
 * connections and creates a {@linkplain TCPServer Server} and
 * a {@linkplain TCPClient Client} Thread for each connection
 */
public class ThreadManager extends Thread {
    /**
     *
     */
    private static final String BROADCAST_ADDR = "255.255.255.255";

    private int port;
    private boolean running = false;

    private ServerSocket servSocket;

    private static UDPHandler udpHandler;

    private static final Logger LOGGER = Logger.getLogger("clavarder.ThreadManager");

    private ArrayList<Thread> threadList = new ArrayList<>();

    /**
     * Creates a thread manager listening for incoming connections on {@code port}
     * and handling the creation and destruction of {@link TCPClient TCPClient} and
     * {@link TCPServer TCPServer}
     * 
     * @param port the port number
     */
    public ThreadManager(int port) {
        this.port = port;
    }

    /**
     * Starts this {@code ThreadManager}
     */
    public void startHandler() {
        try {
            servSocket = new ServerSocket(port);
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops this ThreadManager
     * 
     * @throws IOException
     */
    public void stopHandler() throws IOException {
        running = false;
        servSocket.close();
        this.interrupt();
    }

    private static HashMap<InetAddress, TCPClient> clientTable = new HashMap<>();
    private static HashMap<InetAddress, TCPServer> serverTable = new HashMap<>();

    /**
     * Creates a Client Thread to send messages to specified
     * {@code clientInetAddress}
     * 
     * @param serverPort        Destination port
     * @param serverInetAddress Destination address
     * @throws IOException
     */
    public void createClientThread(int serverPort, InetAddress serverInetAddress) throws IOException {
        TCPClient clientThread = new TCPClient(serverPort, serverInetAddress);
        clientTable.put(serverInetAddress, clientThread);
    }

    /**
     * Creates a {@link UDPHandler} Thread which broadcasts the chosen pseudo,
     * listens for
     * the answers then starts listening for broadcasts from other users
     * 
     * @param firstPseudo Pseudo chosen on start of the Application
     * 
     * @return True if the pseudo is valid
     */
    public synchronized boolean initUDPHandler(String firstPseudo) {
        boolean initialisationValid = true;
        try {
            if (udpHandler == null)
                udpHandler = new UDPHandler();
            UDPHandler.sendMsg(InetAddress.getByName(BROADCAST_ADDR), firstPseudo);
            ArrayList<Pair<String, InetAddress>> onlineUsers = udpHandler.listenForAnswers();
            LOGGER.info(() -> "Finished listening for answers with online users = " + onlineUsers);
            if (onlineUsers == null) {
                initialisationValid = false;
            } else {
                for (Pair<String, InetAddress> pair : onlineUsers) {
                    App.addOnlineUsers(pair.getValue(), pair.getKey());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (initialisationValid) {
            threadList.add(udpHandler);
        }
        return initialisationValid;
    }

    /**
     * Starts the Thread listening for broadcasts in {@link UDPHandler}
     */
    public void startUDPListener() {
        udpHandler.startListener();
    }

    /**
     * Stops the {@link UDPHandler} Thread listening for broadcasts
     */
    public void stopUDPHandler() {
        udpHandler.stopListener();
    }

    public void terminateAllThreads() {
        for (Thread thread : threadList) {
            thread.interrupt();
        }
    }

    /**
     * Closes the Threads (Client and Server) dedicated to communication with
     * {@code address} and remove the corresponding Threads from the Table
     * 
     * @param address
     */
    public static void closeConnectionThreads(InetAddress address) {
        closeClientThread(address);
        closeServerThread(address);
    }

    private static void closeClientThread(InetAddress address) {
        TCPClient client = clientTable.get(address);
        if (client != null) {
            client.stopClient();
            clientTable.remove(address);
        }
    }

    private static void closeServerThread(InetAddress address) {
        TCPServer server = serverTable.get(address);
        if (server != null) {
            server.stopServer();
            serverTable.remove(address);
        }
    }

    /**
     * Called by Server thread to pass a new received {@code msg} coming from
     * {@code senderAddress}
     * 
     * @param msg           Message received via TCP
     * @param senderAddress Address of the sender
     */
    protected static void notifyMessageReceived(String msg, InetAddress senderAddress) {
        App.displayMsg(msg, senderAddress);
    }

    /**
     * Called by Server thread to notify the connection was closed by the remote
     * Client
     * <p>
     * Closes the Client thread, actualises both tables and notifies the Application
     * 
     * @param address Address of the client which ended the connection
     */
    protected static void notifyConnectionClosed(InetAddress address) {
        closeClientThread(address);
        serverTable.remove(address);
    }

    /**
     * Called by UDPHandler while listening to notify a new user has connected,
     * disconnected or wants to change its pseudo
     * 
     * @param content       Content of the broadcast received: either the pseudo or
     *                      "--OFF--" if the user broadcasts its disconection
     * @param senderAddress Address of the broadcasting user
     * @throws UnknownHostException
     */
    protected static void notifyOnlineModif(String content, InetAddress senderAddress) throws UnknownHostException {
        try {
            if ("--OFF--".equals(content)) { // The user has disconnected -> removal from the list
                //
                App.removeOnlineUser(senderAddress);
            } else if (content.equals(App.getPseudo())) {
                Thread.sleep(1000);
                UDPHandler.sendMsg(senderAddress, "--INVALID--");

            } else if (!"--INVALID--".equals(content) && !App.getUserCorresp().containsValue(content)) {
                /*
                 * Flags --INVALID-- need no answering to for obvious reasons.
                 * We don't want to do anything either when a user attempts to connect using a
                 * username already taken by another. ==> Allows the fix the multi-interface
                 * broadcast issue
                 */
                Thread.sleep(1000);
                if (!App.getOnlineUsers().contains(senderAddress)) { // Send own pseudo only if this is a new
                                                                     // user
                    UDPHandler.sendMsg(senderAddress, App.getPseudo());
                    Thread.sleep(3000);
                    App.newDiscussion(senderAddress);
                }
                App.addOnlineUsers(senderAddress, content);
            }
        } catch (
        Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if passed parameter {@code address} is localhost
     * 
     * @param address IP Address to compare with
     * @return True if the IP address passed in argument is the Application
     *         localhost address
     * @throws UnknownHostException If the IP Address of the host could not be
     *                              determined
     */
    public static boolean isAddressLocalhost(InetAddress address) throws UnknownHostException {
        String localhost = InetAddressLocalHostUtil.getLocalHostAsString();
        return localhost.equals(address.getHostAddress());
    }

    /**
     * Broadcasts (UDP) that the user is disconnecting (Sends --OFF-- to all)
     * 
     * @throws IOException
     * @throws UnknownHostException
     */
    public void broadcastDisconnection() throws IOException {
        UDPHandler.sendMsg(InetAddress.getByName(BROADCAST_ADDR), "--OFF--");
    }

    /**
     * Broadcast (UDP) the new username to everyone
     * 
     * @param newUsername New username chosen ("Can't be --OFF--")
     * @throws IOException
     * @throws UnknownHostException
     */
    public void broadcastChangeUsername(String newUsername) throws IOException {
        UDPHandler.sendMsg(InetAddress.getByName(BROADCAST_ADDR), newUsername);
    }

    /**
     * Transmits a {@code msg} to specified {@code receivAddress} (TCP
     * communication)
     * 
     * @param msg           Message to be send through TCP
     * @param receivAddress Destinary IP Address
     */
    protected void transmitMessage(String msg, InetAddress receivAddress) {
        TCPClient client = clientTable.get(receivAddress);
        try {
            client.sendMsg(msg);
        } catch (NullPointerException e) {
            LOGGER.log(Level.WARNING, "Host disconnected", e); // ? @HeineKayn Ca serait possible d'avoir une popup qui
                                                               // dit "l'utilisateur s'est déco"?
            App.removeOnlineUser(receivAddress);
        }
    }

    @Override
    public void run() {
        running = true;
        LOGGER.fine(() -> "Thread Handler started - " + this);
        while (running) {
            try {

                Socket socket = servSocket.accept();

                TCPServer requestHandler = new TCPServer(socket);
                serverTable.put(socket.getInetAddress(), requestHandler); // Adds the Server thread to table
                threadList.add(requestHandler);
                requestHandler.start();
            } catch (Exception e) {
                if (!(e instanceof SocketException))
                    e.printStackTrace();
            }
        }
        LOGGER.fine(() -> "Thread Handler terminated - " + this);
    }

}
