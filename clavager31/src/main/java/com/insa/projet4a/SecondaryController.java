package com.insa.projet4a;

import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    
    /** 
     * @throws IOException
     */
    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}