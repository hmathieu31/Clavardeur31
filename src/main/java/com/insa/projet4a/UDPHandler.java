package com.insa.projet4a;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

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

    private static final int LISTENER_PORT = 50002;

    private static final Logger LOGGER = Logger.getLogger("clavarder.UDPHandler");

    private boolean running;
    private static DatagramSocket broadcasterSocket;

    /**
     * Creates a new UDPHandler listening for broadcasts on port {@code 50002} and
     * emitting on port {@code 50001}
     * 
     * @throws SocketException
     */
    public UDPHandler() throws SocketException {
        super();
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
    public static synchronized void sendMsg(InetAddress destinAddress, String msg) throws IOException {
        if (UDPHandler.broadcasterSocket == null) {
            UDPHandler.broadcasterSocket = new DatagramSocket();
            broadcasterSocket.setBroadcast(true);
        }
        DatagramPacket outPacket;
        if ("255.255.255.255".equals(destinAddress.getHostAddress())) {
            for (InetAddress address : listAllBroadcastAddresses()) {
                outPacket = new DatagramPacket(msg.getBytes(), msg.length(), address,
                        LISTENER_PORT);
                broadcasterSocket.send(outPacket);
                LOGGER.info(() -> "UDP Send - " + msg + " to " + address);
            }
        } else {
            outPacket = new DatagramPacket(msg.getBytes(), msg.length(), destinAddress, LISTENER_PORT);
            broadcasterSocket.send(outPacket);
            LOGGER.info(() -> "UDP Send - " + msg + " to " + destinAddress);
        }
    }

    private static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(InterfaceAddress::getBroadcast)
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
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
    public synchronized ArrayList<Pair<String, InetAddress>> listenForAnswers() throws IOException {
        ArrayList<Pair<String, InetAddress>> onlineUsers = new ArrayList<>();
        byte[] buffer = new byte[256];

        boolean keepListening = true;
        boolean pseudoInvalid = false;
        DatagramSocket listenerInitSocket = new DatagramSocket(LISTENER_PORT);
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        while (keepListening) { // Keep listening until a user answers with "--INVALID--" or until the timer
                                // expires <=> no more answers are expected
            LOGGER.info("Started Listening for answers");
            try {
                listenerInitSocket.setSoTimeout(2000);
                while (keepListening) {
                    listenerInitSocket.receive(inPacket);
                    InetAddress inAddress = inPacket.getAddress();
                    String content = new String(inPacket.getData(), 0, inPacket.getLength());

                    if (!ThreadManager.isAddressLocalhost(inAddress)) {
                        if (!"--OFF--".equals(content) && !"--INVALID--".equals(content)) {
                            LOGGER.info(() -> "User discovered - " + inAddress);
                            onlineUsers.add(new Pair<String, InetAddress>(content, inAddress));
                        }
                        pseudoInvalid = "--INVALID--".equals(content);
                        keepListening = !pseudoInvalid;
                    }
                }
            } catch (SocketTimeoutException timeout) {
                keepListening = false;
            } finally {
                listenerInitSocket.close();
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
        LOGGER.info(() -> "Listener started - " + this);
        byte[] buffer = new byte[256];

        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        try (DatagramSocket listenerRunnableSocket = new DatagramSocket(LISTENER_PORT)) {
            while (running) {
                listenerRunnableSocket.setSoTimeout(0);
                listenerRunnableSocket.receive(inPacket);
                InetAddress inAddress = inPacket.getAddress();

                String content = new String(inPacket.getData(), 0, inPacket.getLength());

                if (!ThreadManager.isAddressLocalhost(inAddress)) { // Ignore all broadcasts coming from oneself
                    LOGGER.info(() -> "Received broadcast from " + inAddress + " - " + content);
                    ThreadManager.notifyOnlineModif(content, inAddress);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info(() -> "Listener terminated - " + this);
    }

}
