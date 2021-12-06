package com.insa.projet4a;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    // S'execute toujours au lancement de la fenetre
    @FXML
    protected void initialize() throws IOException {
        identityLabel.setText(GUI.pseudo);;
        addConnected("Michel");
        addConnected("Jean");
        addConnected("Kevin");

        // ArrayList<Pair<Boolean,String>> list = new ArrayList<Pair<Boolean,String>>();
        // Pair<Boolean,String> p1 = new Pair<Boolean,String>(true,"salut");list.add(p1);
        // Pair<Boolean,String> p2 = new Pair<Boolean,String>(false,"bonjour");list.add(p2);
        // Pair<Boolean,String> p3 = new Pair<Boolean,String>(true,"comment ça va");list.add(p3);
        // Pair<Boolean,String> p4 = new Pair<Boolean,String>(false,"mal");list.add(p4);
        // loadMessages(list);
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
        Label messageLabel = (Label) pane.getChildren().get(0);
        messageLabel.setText(content);
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
        SimpleDateFormat formater = new SimpleDateFormat("Le' dd MMMM 'à' hh:mm:ss");;
        return formater.format(date);
    }

    // Rajoute un message à la discussion actuelle
    // -> Append dans l'historique correspondant
    // -> Transmettre par TCP le message
    @FXML
    private void sendMessage(KeyEvent key) throws IOException {
        if(key.getCode() == KeyCode.ENTER){
            String messageText = messageField.getText();
            if (!messageText.isEmpty()){
                String date = currentDate();
                addMessageTo(date,messageText);
                messageField.clear();
            }  
        }
    }

    private void loadMessages(ArrayList<Triple<Boolean,String>> list) throws IOException{
        for(Pair<Boolean,String> p : list){
            Boolean from = (Boolean) p.getKey();
            String content = (String) p.getValue();

            if (from){
                addMessageFrom(date,content);
            }
            else{
                addMessageTo(date,content);
            }
        }

    }

    // private void loadMessagesOf(int index){
    //     String pseudo = getPseudoFromIndex(index);
    // }

    // Rajoute une entrée (label) au Menu bas gauche 
    // -> Appelé quand on nous broadcast l'existence
    @FXML
    public void addConnected(String content) throws IOException{
        FXMLLoader loader = new FXMLLoader();   
        AnchorPane pane = loader.load(getClass().getResource("components/connected.fxml").openStream());
        pane.setOnMouseClicked(e -> connectToUser(pane));
        
        Label messageLabel = (Label) pane.getChildren().get(0);

        messageLabel.setText(content);
        connectedList.getChildren().add(pane);
    }

    // Passe un user du menu Connected(bas gauche) à celui de DiscussionActuel(haut gauche)
    // -> Il faudra envoyer une requete de connexion TCP
    private void connectToUser(AnchorPane pane){
        Label messageLabel = (Label) pane.getChildren().get(0);
        VBox parent = (VBox)pane.getParent();
        parent.getChildren().remove(pane);

        String user = messageLabel.getText();
        currentDiscussionList.getItems().add(user);
        
        System.out.println(user);
    }

    private String getPseudoFromIndex(int index){
        return currentDiscussionList.getItems().get(index).toString();
    }

    // Quand on clique sur la listeView cela choisi un user (surligné en bleu)
    // A chaque clique on regarde quel est l'utilisateur choisi
    // -> Load l'historique correspondant
    @FXML
    private void updateCurrentDiscussion(){

        if (currentDiscussionList.getSelectionModel().getSelectedIndices().size() > 0){
            GUI.currentDiscussionIndex = (int)currentDiscussionList.getSelectionModel().getSelectedIndices().get(0);
            String name = getPseudoFromIndex(GUI.currentDiscussionIndex);
            resetMessage();

            System.out.println(name);
        } 
    }

    // Quand on appuie sur "Suppr" cela remove un user la liste des gens avec qui on discute
    // Il est ainsi placé dans la liste des gens connectés
    // -> On doit finir la connexion en tcp
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