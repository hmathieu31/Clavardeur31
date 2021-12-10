package com.insa.projet4a;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws UnknownHostException, IOException {
        TCPClient client = new TCPClient(12, Inet4Address.getByName("localhost"));
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        while (!"close".equals(input)) {
            client.sendMsg(input);
            input = scanner.nextLine();
        }
        System.out.println("Closing connexion");
        scanner.close();
    }
}
