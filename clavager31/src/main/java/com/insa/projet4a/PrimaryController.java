package com.insa.projet4a;

import java.io.IOException;
import javafx.fxml.FXML;

public class PrimaryController {

    
    /** 
     * @throws IOException
     */
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
