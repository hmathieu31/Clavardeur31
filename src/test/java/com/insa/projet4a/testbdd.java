package com.insa.projet4a;

import java.sql.SQLException;
import java.util.ArrayList;

public class testbdd {
    
    public static void main(String[] args) throws SQLException {
        BDDManager db = new BDDManager("test");

        db.initHistory();

        // db.insertHistory("user2", false, "content", "date");
        // db.clearHistory();

        ArrayList<Message> list = db.showHistory();
        for (Message message : list) {
            System.out.println(message);
        }
    }
}