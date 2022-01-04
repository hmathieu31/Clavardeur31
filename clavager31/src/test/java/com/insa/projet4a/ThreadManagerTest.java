package com.insa.projet4a;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.junit.jupiter.api.Test;

public class ThreadManagerTest {

    @Test
    void testIsAddressLocalhost() throws UnknownHostException, SocketException {

        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while(en.hasMoreElements()){
            NetworkInterface ni=(NetworkInterface) en.nextElement();
            Enumeration ee = ni.getInetAddresses();
            while(ee.hasMoreElements()){
                InetAddress ia= (InetAddress) ee.nextElement();
                if(!ia.getHostAddress().contains(":") && !ia.getHostAddress().contains("127.0.0.1")){
                    System.out.println(ia.getHostAddress());
                }
            }
        }

        assertTrue(ThreadManager.isAddressLocalhost(InetAddress.getByName("10.32.44.157")));
        assertFalse(ThreadManager.isAddressLocalhost(InetAddress.getByName("192.168.1.12")));
        assertFalse(ThreadManager.isAddressLocalhost(InetAddress.getByName("localhost")));
    }
}
