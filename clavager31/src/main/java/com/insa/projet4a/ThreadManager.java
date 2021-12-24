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
 * The class implements the thread handling of TCP connections on the receiving
 * end
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
     * Starts the server handler
     */
    public void startServer() {
        try {
            servSocket = new ServerSocket(port);
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the Thread Handler
     */
    public void stopHandler() {
        running = false;
        this.interrupt();
    }

    private static HashMap<InetAddress, TCPClient> clientTable = new HashMap<InetAddress, TCPClient>();
    private static HashMap<InetAddress, TCPServer> serverTable = new HashMap<InetAddress, TCPServer>();

    /**
     * Creates a Client Thread to send messages to {@code clientInetAddress}
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
     * Creates a Thread UDPHandler which broadcasts the chosen pseudo, listens for
     * the answers then starts the listening Thread
     * 
     * @param firstPseudo Pseudo chosen on start of the Application
     * 
     * @return True if the pseudo is valid
     */
    public boolean initUDPHandler(String firstPseudo) {

        boolean initialisationValid = true;

        try {
            udpHandler = new UDPHandler();
            UDPHandler.sendMsg(InetAddress.getByName("255.255.255.255"), firstPseudo);
            ArrayList<Pair<String, InetAddress>> onlineUsers = udpHandler.listenForAnswers();
            System.out.println("logpoint initUDPHandler after listening for answers");
            if (onlineUsers == null) {
                initialisationValid = false;
            }
            Thread.sleep(3000);
            udpHandler.startListener();

        } catch (SocketException | UnknownHostException | InterruptedException e) {
            e.printStackTrace();
        }
        return initialisationValid;
    }

    /**
     * Stops the Listener on UDP broadcasts
     */
    public void stopUDPHandler() {
        udpHandler.stopListener();
    }

    /**
     * Closes the Threads (Client and Server) dedicated to communication with
     * {@code address} and remove the Threads from the Table
     * 
     * @param address
     */
    public static void closeConnectionThreads(InetAddress address) {
        closeClientThread(address);
        closeServerThread(address);
    }

    private static void closeClientThread(InetAddress address) {
        TCPClient client = clientTable.get(address);
        client.stopClient();
        clientTable.remove(address);
    }

    private static void closeServerThread(InetAddress address) {
        TCPServer server = serverTable.get(address);
        server.stopServer();
        serverTable.remove(address);
    }

    /**
     * Called by Server thread to pass a new received {@code msg} coming from
     * {@code senderAddress}
     * 
     * @param msg
     * @param senderAddress
     */
    protected static void notifyMessageReceived(String msg, InetAddress senderAddress) {
        App.displayMsg(msg, senderAddress);
    }

    /**
     * Called by Server thread to notify the connection was closed by the remote
     * Client
     * <p>
     * Closes the Client thread and actualises both tables
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
     */
    protected static void notifyOnlineModif(String content, InetAddress senderAddress) {
        if ("--OFF--".equals(content)) { // The user has disconnected -> removal from the list
            App.removeOnlineUser(senderAddress);
        }
        boolean pseudoValid = !content.equals(App.pseudo); // Compare the desired pseudo to the App pseudo
        try {
            if (pseudoValid) { // The pseudo the new user wants to use is valid -> add to list and answer with
                               // NAME
                App.addOnlineUsers(senderAddress, content);
                UDPHandler.sendMsg(senderAddress, App.pseudo);
            } else { // The pseudo chosen by the new user is invalid -> answer INVALID
                UDPHandler.sendMsg(senderAddress, "--INVALID--");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts that the user is disconnecting (Sends --OFF-- to all)
     */
    public void broadcastDisconnection() {
        try {
            UDPHandler.sendMsg(InetAddress.getByName("255.255.255.255"), "--OFF--");
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Transmits a {@code msg} to specified {@code receivAddress}
     * 
     * @param msg
     * @param receivAddress
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
                // System.out.println("Listening for connections on port " + port);

                Socket socket = servSocket.accept();

                TCPServer requestHandler = new TCPServer(socket);
                serverTable.put(socket.getInetAddress(), requestHandler); // Adds the Server thread to table
                requestHandler.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
