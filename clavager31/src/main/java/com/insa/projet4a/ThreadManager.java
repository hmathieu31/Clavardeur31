package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * The class implements the thread handling of TCP connections on the receiving
 * end
 */
public class ThreadManager extends Thread {
    private int port;
    private boolean running = false;

    /**
     * Server socket created on acception of connexion by the server
     */
    private ServerSocket servSocket;

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

    public void stopServer() {
        running = false;
        this.interrupt();
    }

    private HashMap<InetAddress, TCPClient> clientTable = new HashMap<InetAddress, TCPClient>();
    private HashMap<InetAddress, TCPServer> serverTable = new HashMap<InetAddress, TCPServer>();

    
    /** 
     * @param clientPort
     * @param clientInetAddress
     * @throws IOException
     */
    public void createClientThread(int clientPort, InetAddress clientInetAddress) throws IOException {
        TCPClient clientThread;
        clientThread = new TCPClient(clientPort, clientInetAddress);
        clientTable.put(clientInetAddress, clientThread);
        clientThread.run();
    }

    
    /** 
     * Closes the Threads (Client and Server) dedicated to communication with {@code address}
     * @param address
     */
    public void closeConnectionThreads(InetAddress address) {
        TCPClient client = clientTable.get(address);
        client.stopClient();
        TCPServer server = serverTable.get(address);
        server.stopServer();
    }


    
    /** 
     * @param msg
     * @param senderAddress
     */
    protected static void notifyMessageReceived(String msg, InetAddress senderAddress) {
        AppTest.displayMsg(msg, senderAddress);
    }

    
    /** 
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
                System.out.println("Listening for connections on port " + port);

                Socket socket = servSocket.accept();

                TCPServer requestHandler = new TCPServer(socket);
                serverTable.put(socket.getInetAddress(), requestHandler);      // Adds the listener thread to table
                requestHandler.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
