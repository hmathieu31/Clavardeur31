package com.insa.projet4a;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class bddTest {
    
    static BDDManager db;
	
	@BeforeAll
	static void init() {
		db = new BDDManager("test");
	} 

    @Test
    void testConnect() throws SQLException{
        String url = "jdbc:sqlite:test.db";
        DriverManager.getConnection(url);
        assert true;
    }

    @Test
    void testInit() throws SQLException{
        db.initHistory();
    }


    @Test
    void testInsert() throws SQLException{
        db.insertHistory("test", false, "content", "date");
    }

    @Test
    void testHistory() throws SQLException{
        ArrayList<Message> list = db.showHistory();
        for (Message message : list) {
            System.out.println(message);
        }
    }

    @Test
    void testRemove() throws SQLException{
        db.clearHistory("test");
    }
}
