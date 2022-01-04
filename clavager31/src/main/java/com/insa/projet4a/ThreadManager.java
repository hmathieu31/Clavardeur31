package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.util.Pair;

/**
 * The {@code ThreadManager} class defines a Thread Manager
 * <p>
 * A Thread Manager creates a {@link UDPHandler}, listens for incoming
 * connections and creates a {@linkplain TCPServer Server} and
 * a {@linkplain TCPClient Client} Thread for each connection
 */
public class ThreadManager extends Thread {
    private int port;
    private boolean running = false;

    private ServerSocket servSocket;

    private static UDPHandler udpHandler;

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
     * @throws IOException
     */
    public void stopHandler() throws IOException {
        running = false;
        servSocket.close();
        this.interrupt();
    }

    private static HashMap<InetAddress, TCPClient> clientTable = new HashMap<InetAddress, TCPClient>();
    private static HashMap<InetAddress, TCPServer> serverTable = new HashMap<InetAddress, TCPServer>();

    /**
     * Creates a Client Thread to send messages to specified
     * {@code clientInetAddress}
     * 
     * @param clientPort        Destination port
     * @param clientInetAddress Destination address
     * @throws IOException
     */
    public void createClientThread(int clientPort, InetAddress clientInetAddress) throws IOException {
        TCPClient clientThread;
        clientThread = new TCPClient(clientPort, clientInetAddress);
        clientTable.put(clientInetAddress, clientThread);
        clientThread.run();
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
    public boolean initUDPHandler(String firstPseudo) {

        boolean initialisationValid = true;

        try {
            if (udpHandler == null)
                udpHandler = new UDPHandler();
            UDPHandler.sendMsg(InetAddress.getByName("255.255.255.255"), firstPseudo);
            ArrayList<Pair<String, InetAddress>> onlineUsers = udpHandler.listenForAnswers();
            if (onlineUsers == null) {
                initialisationValid = false;
            } else {
                for (Pair<String, InetAddress> pair : onlineUsers) {
                    App.addOnlineUsers(pair.getValue(), pair.getKey());
                }
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
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
        App.notifyConnectionClosed(address);
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

        boolean pseudoFree = !content.equals(App.getPseudo()); // Compare the desired pseudo to the App pseudo
        try {
            if ("--OFF--".equals(content)) { // The user has disconnected -> removal
                // from the list
                App.removeOnlineUser(senderAddress);
            } else if (!"--INVALID--".equals(content)) { // Ignore --INVALID--
                                                         // messages
                                                         // and messages from
                                                         // localhost
                if (pseudoFree) {
                    if (!App.getOnlineUsers().contains(senderAddress)) { // Send own pseudo only if this is a new user
                        UDPHandler.sendMsg(senderAddress, App.getPseudo());
                    }
                    App.addOnlineUsers(senderAddress, content);
                } else { // The pseudo chosen by the new user is taken -> answer INVALID
                    UDPHandler.sendMsg(senderAddress, "--INVALID--");
                }
            }
        } catch (Exception e) {
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
     */
    public void broadcastDisconnection() {
        try {
            UDPHandler.sendMsg(InetAddress.getByName("255.255.255.255"), "--OFF--");
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcast (UDP) the new username to everyone
     * 
     * @param newUsername New username chosen ("Can't be --OFF--")
     */
    public void broadcastNewUsername(String newUsername) {
        try {
            UDPHandler.sendMsg(InetAddress.getByName("255.255.255.255"), newUsername);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
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
        client.sendMsg(msg);
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                // ! System.out.println("Listening for connections on port " + port);

                Socket socket = servSocket.accept();

                TCPServer requestHandler = new TCPServer(socket);
                serverTable.put(socket.getInetAddress(), requestHandler); // Adds the Server thread to table
                requestHandler.start();
            } catch (Exception e) {
                if(!(e instanceof SocketException))
                e.printStackTrace();
            }
        }
    }

}
