package com.insa.projet4a;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
public class LoginController {

    Alert alert = new Alert(AlertType.ERROR, 
                        "This name is taken, try again with another one.", 
                        ButtonType.OK);
    
    @FXML private TextField pseudoField;

    @FXML
    private void connect(KeyEvent key) throws IOException { // TODO Handle conflicts with App.connect
        if(key.getCode() == KeyCode.ENTER){
            String pseudo = pseudoField.getText();

            ArrayList<String> pseudoList = new ArrayList<String>();
            Collections.addAll(pseudoList, "Hugo", "Thomas", "Etchebest");

            if (!pseudoList.contains(pseudo)){
                App.setPseudo(pseudo);
                App.setRoot("main");
                App.changeSize(1000, 800);
            }   
            else{
                pseudoField.clear();
                alert.show();
            }
        }
    }
}
