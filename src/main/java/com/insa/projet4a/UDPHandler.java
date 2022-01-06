package com.insa.projet4a;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javafx.util.Pair;

/**
 * A UDPHandler Thread is created by the {@link ThreadManager} to listen to
 * broadcasts
 * of new
 * connection or changes of pseudo by other users.
 * <p>
 * It also broadcasts the pseudo chosen by the user when the Application is
 * started, when the user wishes to change its pseudo and when the Application
 * is closed
 */
public class UDPHandler extends Thread {
    private static final int portBroadcaster = 50001;
    private static final int portListener = 50002;

    private boolean running;
    private static DatagramSocket broadcasterSocket;

    /**
     * Creates a new UDPHandler listening for broadcasts on port {@code 60000} and
     * emitting on port {@code 60001}
     * 
     * @throws SocketException
     */
    public UDPHandler() throws SocketException {
        super();
        broadcasterSocket = new DatagramSocket(portBroadcaster);
    }

    /**
     * Stops this UDPHandler
     */
    public void stopListener() {
        running = false;
        broadcasterSocket.close();
        this.interrupt();
    }

    /**
     * Starts this UDPHandler
     */
    public void startListener() {
        running = true;
        this.start();
    }

    /**
     * Sends a {@code msg} using UDP to {@code destinAddress}
     * 
     * @param destinAddress Address of the destinary
     * @param msg           Message to pass
     * @throws IOException
     */
    public synchronized static void sendMsg(InetAddress destinAddress, String msg) throws IOException {
        DatagramPacket outPacket = new DatagramPacket(msg.getBytes(), msg.length(), destinAddress, portListener);
        System.out.println("UDP sending to " + destinAddress);
        broadcasterSocket.send(outPacket);
        System.out.println("UDP sending - " + msg);
    }

    /**
     * Called by Thread Manager after a first connection broadcast with given
     * pseudo.
     * 
     * Listens for the usernames of everyone connected or --INVALID-- if the given
     * pseudo was already taken.
     * 
     * @return The list of all connected users and their pseudo ; Or {@code null}
     *         if the pseudo is already taken
     * @throws IOException
     */
    public ArrayList<Pair<String, InetAddress>> listenForAnswers() throws IOException {
        ArrayList<Pair<String, InetAddress>> onlineUsers = new ArrayList<Pair<String, InetAddress>>();
        byte[] buffer = new byte[256];

        boolean keepListening = true;
        boolean pseudoInvalid = false;
        DatagramSocket listenerInitSocket = new DatagramSocket(portListener);
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        while (keepListening) { // Keep listening until a user answers with "--INVALID--" or until 10s have
                                // expired <=> no more answers are expected
            try {
                listenerInitSocket.setSoTimeout(50);
                while (keepListening) {
                    listenerInitSocket.receive(inPacket);
                    InetAddress inAddress = inPacket.getAddress();
                    String content = new String(inPacket.getData(), 0, inPacket.getLength());

                    if (!ThreadManager.isAddressLocalhost(inAddress)) {
                        if (!"--OFF--".equals(content) && !"--INVALID--".equals(content)) {
                            onlineUsers.add(new Pair<String, InetAddress>(content, inAddress));
                        }
                        pseudoInvalid = "--INVALID--".equals(content);
                        keepListening = !pseudoInvalid;
                    }
                }
            } catch (SocketTimeoutException timeout) {
                keepListening = false;
            }
        }
        listenerInitSocket.close();
        if (pseudoInvalid) { // If the loop was exited due to invalid pseudo, onlineUsers = null
            onlineUsers = null;
        }
        return onlineUsers;
    }

    @Override
    public void run() {
        System.out.println("Listener started");
        byte[] buffer = new byte[256];

        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        try {
            DatagramSocket listenerRunnableSocket = new DatagramSocket(portListener);
            while (running) {
                listenerRunnableSocket.setSoTimeout(0);
                listenerRunnableSocket.receive(inPacket);
                InetAddress inAddress = inPacket.getAddress();

                String content = new String(inPacket.getData(), 0, inPacket.getLength());

                if (!ThreadManager.isAddressLocalhost(inAddress)) { // Ignore all broadcasts coming from oneself
                    System.out.println("Received broadcast from " + inAddress + " - " + content);
                    ThreadManager.notifyOnlineModif(content, inAddress);
                }
            }
            listenerRunnableSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // super.run();
    }

}