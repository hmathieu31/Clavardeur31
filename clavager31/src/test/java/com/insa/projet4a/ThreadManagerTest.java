package com.insa.projet4a;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

public class ThreadManagerTest {

    @Test
    void testIsAddressLocalhost() throws UnknownHostException {
        System.out.println(InetAddressLocalHostUtil.getLocalHost());
        assertTrue(ThreadManager.isAddressLocalhost(InetAddress.getByName("10.32.46.153")));
        assertFalse(ThreadManager.isAddressLocalhost(InetAddress.getByName("192.168.1.12")));
        assertFalse(ThreadManager.isAddressLocalhost(InetAddress.getByName("localhost")));
    }
}
