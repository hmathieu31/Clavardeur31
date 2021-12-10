package com.insa.projet4a;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

class TCPServer extends Thread {
    private Socket socket;

    TCPServer(Socket socket) {
        super();
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Received a connection");

            // Get input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Echo lines back to the client until the client closes the connection or we
            // receive an empty line
            String line = in.readLine();
            while (!"--END CONNECTION--".equals(line)) {
                    ThreadManager.notifyMessageReceived(line, socket.getInetAddress());
                    line = in.readLine();
            }

            // Close our connection
            in.close();
            socket.close();

            System.out.println("Connection closed");
        } catch (Exception e) {
            System.out.println("Connection closed by client");
        }
    }

}