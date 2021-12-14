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
     * * Temporary boolean (used only for testing purposes as a simplification of
     * the list containing all ongoing communications)
     */
    private static boolean isDiscussionOngoing;

    /**
     * <p>
     * Function called when the application is started.
     * </p>
     * <p>
     * Starts a ThreadManager listening for incoming communications
     * on port 12.
     * </p>
     */
    public static void connect() {
        threadManager = new ThreadManager(12);
        threadManager.startServer();
        System.out.println("Waiting for connexion on port 12");
        System.out.println();
    }

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
            threadManager.createClientThread(12, receivAddress);
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
     * Called by the Thread Manager to notify the Application that {@code address}
     * closed the connection
     * <p>
     * ! Will be changed to call the GUI
     * 
     * @param address address of the remote client
     */
    public static void notifyConnectionClosed(InetAddress address) {
        System.out.println("Connection closed by remote initiative with " + address);
        isDiscussionOngoing = false;
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
        System.out.println("Connexion closed by local initiative with " + receivAddress);
    }

    /**
     * @param args
     * @throws UnknownHostException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        InetAddress receivAddress = InetAddress.getByName("192.168.1.84"); // Address of the receiver (localhost for the
                                                                           // purposes of testing)

        connect();

        while (newDiscussion(receivAddress) == false) {
            Thread.sleep(1000);
        }
        isDiscussionOngoing = true;
        Scanner scanner = new Scanner(System.in);
        String txt;

        do {
            txt = scanner.nextLine();
            transmitMessage(txt, receivAddress);
        } while (isDiscussionOngoing && !"close".equals(txt));


        scanner.close();

        endDiscussion(receivAddress);

        threadManager.stopServer(); // Only for testing purposes. In real use, there is no reason to stop the
                                    // threadManager just because a connection was closed
    }

}