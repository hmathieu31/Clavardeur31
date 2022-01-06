package com.insa.projet4a;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {

    @FXML
    Alert alert = new Alert(AlertType.ERROR,
            "This name is taken, try again with another one.",
            ButtonType.OK);

    @FXML
    private TextField pseudoField;

    @FXML
    private void connect(KeyEvent key) throws IOException {
        if (key.getCode() == KeyCode.ENTER) {
            String pseudo = pseudoField.getText();

            App.setPseudo(pseudo); // The App username is set, regardless of whether the chosen username is valid
                                   // to allow notifyOnlineModifs to work properly

            boolean pseudoValid = App.isInitPseudoValid(pseudo);

            if (pseudoValid) {
                App.setRoot("main");
                App.changeSize(1000, 800);
            } else {
                pseudoField.clear();
                App.setPseudo(null);
                alert.show();
            }
        }
    }
}
