package com.insa.projet4a;

public class Server {
    public static void main(String[] args) {
        ThreadManager threadHandler = new ThreadManager(12);
        threadHandler.startServer();
        // threadHandler.stopServer();
    }
}
