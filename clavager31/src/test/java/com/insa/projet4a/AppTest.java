package com.insa.projet4a;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

public class AppTest {

    @Test
    void testAddUserCorresp() {
        App.addUserCorresp("192.168.13.13", "toto");
        App.addUserCorresp("128.135.165.198", "zitron");

        HashMap<String, String> mapTest = new HashMap<String, String>();
        mapTest.put("192.168.13.13", "toto");
        mapTest.put("128.135.165.198", "zitron");

        assertEquals("toto", App.getUserCorresp("192.168.13.13"));
        assertEquals("zitron", App.getUserCorresp("128.135.165.198"));
        assertNotEquals(App.getUserCorresp("iuiuhfzef"), "zitron");
        assertEquals(App.getUserCorresp("iugerhger"), null);

    }

    @Test
    void testRemoveUserCorresp() {
        App.addUserCorresp("192.168.13.13", "toto");
        App.addUserCorresp("128.135.165.198", "zitron");

        assertEquals(App.getUserCorresp("192.168.13.13"), "toto");
        App.removeUserCorresp("192.168.13.13");
        assertEquals(App.getUserCorresp("192.168.13.13"), null);

        App.removeUserCorresp("zitrlogon"); // Testing a case where one tries to remove an non-existent element
    }

    @Test
    void testIsPseudoValid() {
        App.addUserCorresp("192.168.10.10", "titi");

        assertTrue(App.isPseudoValid("toto"));
        assertFalse(App.isPseudoValid("titi"));
        App.setPseudo("foo");
        assertTrue(App.isPseudoValid("foo"));

        assertFalse(App.isPseudoValid("--OFF--"));
        assertFalse(App.isPseudoValid("--INVALID--"));
    }
}
