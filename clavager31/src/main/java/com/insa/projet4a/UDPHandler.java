package com.insa.projet4a;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
    private static final int portBroadcaster = 13;
    private static final int portListener = 14;

    private DatagramSocket listenerSocket;
    private boolean running;
    private static DatagramSocket broadcasterSocket;

    /**
     * Creates a new UDPHandler listening for broadcasts on port {@code 13} and
     * emitting on port {@code 14}
     * 
     * @throws SocketException
     */
    public UDPHandler() throws SocketException {
        super();
        this.listenerSocket = new DatagramSocket(portListener);
        broadcasterSocket = new DatagramSocket(portBroadcaster);
    }

    /**
     * Stops this UDPHandler
     */
    public void stopListener() {
        running = false;
        listenerSocket.close();
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
     * @throws SocketException if the Socket could not be opened or bound to the
     *                         local port (13)
     */
    public static void sendMsg(InetAddress destinAddress, String msg) throws SocketException {
        DatagramPacket outPacket = new DatagramPacket(msg.getBytes(), msg.length(), destinAddress, portListener);
        try {
            broadcasterSocket.send(outPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
     */
    public ArrayList<Pair<String, InetAddress>> listenForAnswers() {
        ArrayList<Pair<String, InetAddress>> onlineUsers = new ArrayList<Pair<String, InetAddress>>();
        byte[] buffer = new byte[256];

        boolean keepListening = true;
        boolean pseudoInvalid = false;
        try {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
            while (keepListening) { // Keep listening until a user answers with "--INVALID--" or until 10s have
                                    // expired <=> no more answers are expected
                try {
                    listenerSocket.setSoTimeout(7000);
                    while (keepListening) {
                        listenerSocket.receive(inPacket);

                        InetAddress inAddress = inPacket.getAddress();
                        String content = new String(inPacket.getData(), 0, inPacket.getLength());

                        onlineUsers.add(new Pair<String, InetAddress>(content, inAddress));

                        keepListening = !"--INVALID--".equals(content);
                    }

                } catch (SocketException timeout) {
                    keepListening = false;
                }
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }
        if (pseudoInvalid) { // If the loop was exited due to invalid pseudo, onlineUsers is null
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
            while (running) {
                DatagramSocket listenerRunnableSocket = listenerSocket;
                
                listenerRunnableSocket.setSoTimeout(0);
                listenerRunnableSocket.receive(inPacket);
                InetAddress inAddress = inPacket.getAddress();

                String content = new String(inPacket.getData(), 0, inPacket.getLength());
                ThreadManager.notifyOnlineModif(content, inAddress);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        super.run();
    }

}
