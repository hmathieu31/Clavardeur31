package com.insa.projet4a;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * JavaFX App
 */
public class AppTest {

    private static ThreadManager threadManager;

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
            threadManager.createClientThread(13, receivAddress);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to establish connexion with target");
            return false;
        }
    }

    /**
     * Display a received message
     * ! Will be changed to call the GUI
     * 
     * @param msg     Message received
     * @param address Address of the sender
     */
    public static void displayMsg(String msg, InetAddress address) {
        System.out.println("Msg received from " + address + " --- " + msg);
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
     * ! Set as private and encapsulate into endDisscussionGlobal
     * 
     * @param receivAddress
     */
    public static void endDiscussion(InetAddress receivAddress) {
        transmitMessage("--END CONNECTION--", receivAddress);
        threadManager.endClientThread(receivAddress);
        threadManager.stopServer();
        System.out.println("Connexion closed with " + receivAddress);
    }

    /**
     * <p>
     * Function called when the application is started.
     * </p>
     * Starts a ThreadManager listening for incoming communications
     * on port 12.
     */
    public static void connect() {
        threadManager = new ThreadManager(12);
        threadManager.startServer();
        System.out.println("Waiting for connexion on port 12");
        System.out.println();
    }

    
    /** 
     * @param args
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        InetAddress receivAddress = InetAddress.getLocalHost();

        connect();

        while (newDiscussion(receivAddress) == false) {
            Thread.sleep(1000);
        }

        Scanner scanner = new Scanner(System.in);
        String txt = scanner.nextLine();

        while (!"close".equals(txt)) {
            transmitMessage(txt, receivAddress);
            txt = scanner.nextLine();
        }
        scanner.close();
        endDiscussion(receivAddress);
    }

}