package com.insa.projet4a;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

    @FXML private ListView<String> connectedList;

    Alert alert = new Alert(AlertType.ERROR,
                        "Vous n'avez pas de discussion active, veuillez choisir un utilisateur avec qui communiquer.", 
                        ButtonType.OK);

    @FXML private Button eraseHistory;

    BDDManager bdd;

    // S'execute toujours au lancement de la fenetre
    @FXML
    protected void initialize() throws IOException, SQLException {

        this.bdd = new BDDManager("test");
        this.bdd.initHistory();

        identityLabel.setText(GUI.pseudo);
        addConnected("Michel");
        addConnected("Jean");
        addConnected("Kevin");

        ArrayList<Message> list = new ArrayList<Message>();
        // list.add(new Message(true,currentDate(),"Bonjour"));
        // list.add(new Message(false,currentDate(),"Salut"));
        // list.add(new Message(true,currentDate(),"Comment ça va ? \nQuoi de neuf"));
        // list.add(new Message(false,currentDate(),"Ca va bien.\nRien de spéciale\nEnfin si :  j'ai fini le GUI"));
        list.add(new Message(true,currentDate(),"Bienvenue dans Clavager31"));
        list.add(new Message(true,currentDate(),"Pour envoyer un message veuillez ajouter un utilisateur à vos discussions actives\nSelectionnez ensuite dans cette liste un utilisateur avec qui discuter."));
        loadMessages(list);
    }

    // Pour changer de pseudo
    @FXML
    private void changeIdentity() throws IOException {
        GUI.setRoot("login_screen");
        GUI.changeSize(650, 450);
    }

    // Ajoute un message à l'affichage actuel
    // Cette fonction n'est jamais utilisé d'elle même
    // On utilise à chaque fois 
        // addMessageFrom ou
        // addMessageTo
    @FXML
    private void addMessage(String date, String content, String path) throws IOException{
        FXMLLoader loader = new FXMLLoader();   
        AnchorPane pane = loader.load(getClass().getResource(path).openStream());
        VBox vbox = (VBox) pane.getChildren().get(0);

        AnchorPane pane1 = (AnchorPane) vbox.getChildren().get(0);
        Label messageLabel = (Label) pane1.getChildren().get(0);
        messageLabel.setText(content);

        AnchorPane pane2 = (AnchorPane) vbox.getChildren().get(1);
        Label dateLabel = (Label) pane2.getChildren().get(0);
        dateLabel.setText(date);
        
        messageList.getChildren().add(pane);
    }

    public void addMessageFrom(String date, String content) throws IOException{
        addMessage(date, content, "components/messageFrom.fxml");
    }

    public void addMessageTo(String date, String content) throws IOException{
        addMessage(date, content, "components/messageTo.fxml");
    }

    // Efface tous les messages de la discussion actuelle
    public void resetMessage(){
        messageList.getChildren().clear();
    }

    // Quand on envoie un message on récupére la date du jour
    // On la formate sous une forme jolie
    public String currentDate(){
        Date date = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("'Le' dd MMMM 'à' HH:mm");;
        return formater.format(date);
    }

    // Rajoute un message à la discussion actuelle
    // -> Append dans l'historique correspondant
    // -> Transmettre par TCP le message
    @FXML
    private void sendMessage(KeyEvent key) throws IOException, SQLException {
        if(key.getCode() == KeyCode.ENTER){
            String messageText = messageField.getText();

            if (!messageText.isEmpty()){

                // On vérifie qu'on discute bien avec quelqu'un
                // Pour éviter d'avoir supprimé quelqu'un et continuer de discuter avec

                if (connectedList.getSelectionModel().getSelectedIndices().size() > 0){
                    GUI.currentDiscussionIndex = (int)connectedList.getSelectionModel().getSelectedIndices().get(0);
                    String date = currentDate();
                    addMessageTo(date,messageText);
                    messageField.clear();

                    // A MODIFIER METTRE IP A LA PLACE
                    String pseudoDest = getPseudoFromIndex(GUI.currentDiscussionIndex);
                    this.bdd.insertHistory(pseudoDest, false, messageText, date);
                }
                else{
                    alert.show();
                }
            }  
        }
    }

    // Utilisé quand on change de fenetre ou quand on prend l'historique
    private void loadMessages(ArrayList<Message> list) throws IOException{
        for(Message m : list){
            Boolean from = m.getFrom();
            String content = m.getContent();
            String date = m.getDate();

            if (from){
                addMessageFrom(date,content);
            }
            else{
                addMessageTo(date,content);
            }
        }

    }

    public void addConnected(String name){
        connectedList.getItems().add(name);
    }

    public void removeConnected(String name){
        connectedList.getItems().remove(name);
    }

    private String getPseudoFromIndex(int index){
        return connectedList.getItems().get(index).toString();
    }

    // Quand on clique sur la listeView cela choisi un user (surligné en bleu)
    // A chaque clique on regarde quel est l'utilisateur choisi
    // -> Load l'historique correspondant
    @FXML
    private void updateCurrentDiscussion() throws SQLException, IOException{

        if (connectedList.getSelectionModel().getSelectedIndices().size() > 0){
            GUI.currentDiscussionIndex = (int)connectedList.getSelectionModel().getSelectedIndices().get(0);
            String name = getPseudoFromIndex(GUI.currentDiscussionIndex);

            resetMessage();

            // MODIFIER METTRE IP AU LIEU DE NOM
            // FAUT CORRESP IP/NOM
            ArrayList<Message> history = this.bdd.showHistory(name);
            loadMessages(history);

            System.out.println(name);
        } 
    }

    @FXML 
    private void clearHistory() throws SQLException{

        if (connectedList.getSelectionModel().getSelectedIndices().size() > 0){
            int index = (int)connectedList.getSelectionModel().getSelectedIndices().get(0);
            String name = getPseudoFromIndex(index);

            resetMessage();

            // MODIFIER METTRE IP AU LIEU DE NOM
            // FAUT CORRESP IP/NOM
            this.bdd.clearHistory(name);
        }
        else{
            alert.show();
        }
    }
}