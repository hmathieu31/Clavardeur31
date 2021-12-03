package com.insa.projet4a;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class MainController {

    @FXML private MenuItem changeIdentityButton;

    @FXML
    private void changeIdentity() throws IOException {
        // System.out.println("AAAAA");
        GUI.setRoot("login_screen");
        GUI.changeSize(650, 450);
    }
}