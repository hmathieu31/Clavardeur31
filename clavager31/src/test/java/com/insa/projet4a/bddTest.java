package com.insa.projet4a;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class bddTest {
    
    static BDDManager db;
	
	@BeforeAll
	static void init() {
		db = new BDDManager("test");
	} 

    @Test
    void testConnect(){
        db.connect();
    }

    
}
