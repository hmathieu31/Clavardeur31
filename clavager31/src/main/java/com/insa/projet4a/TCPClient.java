package com.insa.projet4a;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This class implements a TCP client-side connection handler, who connects to the
 * server and sends the messages
 */
public class TCPClient extends Thread {
    /**
     * Port on which the connexion is implemented
     */
    private int port;

    /**
     * Address with which communicate
     */
    private InetAddress address;

    /**
     * Socket on the client used for the connexion
     */
    private Socket socket;

    /**
     * Print writer used to send input through socket to the server
     */
    private PrintWriter pWriter;

    private boolean running;
    

    /**
     * Creates a client-side connection with the machine identified by
     * {@code address} on {@code port}
     * 
     * @param port    Port on which to initiate connection
     * @param inetAddress Address with which to communicate
     * @throws IOException
     */
    public TCPClient(int port, InetAddress inetAddress) throws IOException {
        super();
        this.port = port;
        this.address = inetAddress;
        this.socket = new Socket(address, port);
        this.pWriter = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        while (running) {
            
        }
    }

    /**
     * Get the {@code port} on which the client emits
     * 
     * @return the {@code port}
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the {@code address} to which the client is connected
     * 
     * @return the {@code adress}
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Sends message {@code msg} to server
     * 
     * @param msg
     */
    public void sendMsg(String msg) {
        this.pWriter.println(msg);
    }

    
    public void stopClient() {
        pWriter.close();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        running = false;
        this.interrupt();   
    }

}
