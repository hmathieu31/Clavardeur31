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

import javafx.stage.Stage;
import javafx.scene.Node;

public class LoginController {

    Alert alert = new Alert(AlertType.ERROR, 
                        "This name is taken, try again with another one.", 
                        ButtonType.OK);
    
    @FXML private TextField pseudoField;

    @FXML
    private void connect(KeyEvent key) throws IOException {
        if(key.getCode() == KeyCode.ENTER){
            String pseudo = pseudoField.getText();

            ArrayList<String> pseudoList = new ArrayList<String>();
            Collections.addAll(pseudoList, "Hugo", "Thomas", "Etchebest");

            if (!pseudoList.contains(pseudo)){
                System.out.println(pseudo);

                Stage stage = (Stage)((Node)key.getSource()).getScene().getWindow();

                // Je veux changer de scene et passer en parametre le pseudo
                // Voir vidéo yt
                GUI.changeScene(stage,pseudo,1000,800);
            }   
            else{
                pseudoField.clear();
                alert.show();
            }
        }
    }
}
