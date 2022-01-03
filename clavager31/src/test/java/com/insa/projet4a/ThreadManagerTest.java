package com.insa.projet4a;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.jupiter.api.Test;

public class ThreadManagerTest {

    @Test
    void testIsAddressLocalhost() throws UnknownHostException {
        assertTrue(ThreadManager.isAddressLocalhost(InetAddress.getByName("192.168.1.10")));
        assertFalse(ThreadManager.isAddressLocalhost(InetAddress.getByName("192.168.1.12")));
        assertFalse(ThreadManager.isAddressLocalhost(InetAddress.getByName("localhost")));
    }
}
