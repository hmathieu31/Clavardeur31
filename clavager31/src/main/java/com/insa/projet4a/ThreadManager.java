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

    public void createClientThread(int clientPort, InetAddress clientInetAddress) throws IOException {
        TCPClient clientThread;
        clientThread = new TCPClient(clientPort, clientInetAddress);
        clientTable.put(clientInetAddress, clientThread);
        clientThread.run();
    }

    public void endClientThread(InetAddress address) {
        TCPClient client = clientTable.get(address);
        client.stopClient();
    }


    protected static void notifyMessageReceived(String msg, InetAddress senderAddress) {
        AppTest.displayMsg(msg, senderAddress);
    }

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
                requestHandler.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
