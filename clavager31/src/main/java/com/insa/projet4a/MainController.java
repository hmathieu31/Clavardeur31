package com.insa.projet4a;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private Button changeIdentityButton;
    @FXML private Label identityLabel;

    @FXML private VBox messageList;
    @FXML private TextField messageField;

    @FXML private VBox connectedList;

    @FXML private ListView<String> currentDiscussionList;

    @FXML
    protected void initialize() throws IOException {
        identityLabel.setText(GUI.pseudo);
        addMessageTo("huitre");
        addMessageFrom("huitre");
        addConnected("Michel");
        addConnected("Jean");
        addConnected("Kevin");
    }

    @FXML
    private void changeIdentity() throws IOException {
        GUI.setRoot("login_screen");
        GUI.changeSize(650, 450);
    }

    @FXML
    private void addMessage(String content, String path) throws IOException{
        FXMLLoader loader = new FXMLLoader();   
        AnchorPane pane = loader.load(getClass().getResource(path).openStream());
        Label messageLabel = (Label) pane.getChildren().get(0);
        messageLabel.setText(content);
        messageList.getChildren().add(pane);
    }

    public void addMessageFrom(String content) throws IOException{
        addMessage(content, "components/messageFrom.fxml");
    }

    public void addMessageTo(String content) throws IOException{
        addMessage(content, "components/messageTo.fxml");
    }

    public void resetMessage(){
        messageList.getChildren().clear();
    }

    @FXML
    private void sendMessage(KeyEvent key) throws IOException {
        if(key.getCode() == KeyCode.ENTER){
            String messageText = messageField.getText();
            if (!messageText.isEmpty()){
                addMessageTo(messageText);
                messageField.clear();
            }  
        }
    }

    @FXML
    private void addConnected(String content) throws IOException{
        FXMLLoader loader = new FXMLLoader();   
        AnchorPane pane = loader.load(getClass().getResource("components/connected.fxml").openStream());
        pane.setOnMouseClicked(e -> connectToUser(pane));
        Label messageLabel = (Label) pane.getChildren().get(0);
        messageLabel.setText(content);
        connectedList.getChildren().add(pane);
    }

    private void connectToUser(AnchorPane pane){
        Label messageLabel = (Label) pane.getChildren().get(0);
        VBox parent = (VBox)pane.getParent();
        parent.getChildren().remove(pane);

        String user = messageLabel.getText();
        addCurrentDiscussion(user);
        
        System.out.println(user);
    }
    
    private void addCurrentDiscussion(String user){
        currentDiscussionList.getItems().add(user);
    }

    private String getPseudoFromIndex(int index){
        return currentDiscussionList.getItems().get(index).toString();
    }

    @FXML
    private void updateCurrentDiscussion(){

        if (currentDiscussionList.getSelectionModel().getSelectedIndices().size() > 0){
            GUI.currentDiscussionIndex = (int)currentDiscussionList.getSelectionModel().getSelectedIndices().get(0);
            String name = getPseudoFromIndex(GUI.currentDiscussionIndex);
            resetMessage();
            System.out.println(name);
        } 
    }

    @FXML
    private void removeCurrentDiscussion(KeyEvent key) throws IOException {
        if(key.getCode() == KeyCode.DELETE){
            if (currentDiscussionList.getSelectionModel().getSelectedIndices().size() > 0){
                int index = (int)currentDiscussionList.getSelectionModel().getSelectedIndices().get(0);
                String pseudo = getPseudoFromIndex(index);
                addConnected(pseudo);
                currentDiscussionList.getItems().remove(index);
            }
        }
    }
}