package com.insa.projet4a;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;

class TCPServer extends Thread {
    private Socket socket;
    private boolean running;

    private static final Logger LOGGER = Logger.getLogger("clavarder.TCPServer");

    TCPServer(Socket socket) {
        super();
        this.socket = socket;
    }

    /**
     * Stops the current Server thread
     */
    public void stopServer() {
        running = false;
    }

    @Override
    public void run() {
        try {
            LOGGER.info(() -> "Received a connection from " + this.socket.getInetAddress());
            running = true;

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line = in.readLine();
            while (running && line != null) {
                ThreadManager.notifyMessageReceived(line, socket.getInetAddress());
                line = in.readLine();
            }
            if (line == null && running) { // Case where the connection was closed by remote client and not local
                ThreadManager.notifyConnectionClosed(socket.getInetAddress());
            }
            in.close();
            socket.close();
            this.interrupt();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}