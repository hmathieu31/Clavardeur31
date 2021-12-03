package com.insa.projet4a;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainController {

    @FXML private Button changeIdentityButton;
    @FXML private Label identityLabel;

    @FXML
    protected void initialize() {
        identityLabel.setText(GUI.pseudo);
    }

    @FXML
    private void changeIdentity() throws IOException {
        GUI.setRoot("login_screen");
        GUI.changeSize(650, 450);
    }
}