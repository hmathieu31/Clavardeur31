package com.insa.projet4a;

import java.io.IOException;
import javafx.fxml.FXML;

public class LoginController {
    
    @FXML
    private void connect() throws IOException {
        App.setRoot("primary");
    }
}
