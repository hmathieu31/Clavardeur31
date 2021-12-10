package com.insa.projet4a;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class bddTest {
    
    static BDDManager bdd;
	
	@BeforeAll
	static void init() {
		bdd = new BDDManager("test");
	} 

    @Test
    void testConnect() throws SQLException{
        String url = "jdbc:sqlite:src/main/resources/com/insa/projet4a/sqlite/db/test.db";
        DriverManager.getConnection(url);
        assert true;
    }
}
