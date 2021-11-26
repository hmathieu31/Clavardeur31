package com.insa.projet4a;

import java.net.Inet4Address;

/**
 * This class implements the connexion between 'the user and another application
 */
public class TCPConnection {
    /**
     * Socket on which the connexion is implemented
     */
    int socket;

    int backlog;

    /**
     * Address to which communicate
     */
    Inet4Address address;


    public TCPConnection(int socket, int backlog, Inet4Address address) {
        this.socket = socket;
        this.backlog = backlog;
        this.address = address;
    }

    

}
