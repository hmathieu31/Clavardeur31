package com.insa.projet4a;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML private Button changeIdentityButton;
    @FXML private Label identityLabel;

    @FXML private VBox messageContainer;
    @FXML private TextField messageField;
    @FXML private ScrollPane scrollMessage; 

    @FXML private VBox connectedContainer;

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
        addConnected(new User(0, "localhost", "Michel"));
        addConnected(new User(1, "localhost", "Jean"));
        addConnected(new User(2, "localhost", "Kevin"));
        addConnected(new User(3, "localhost", "Sebastien"));

        ArrayList<Message> list = new ArrayList<Message>();
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
        
        messageContainer.getChildren().add(pane);

        // bizarre il se trigger avant le message ?
        scrollMessage.applyCss();
        scrollMessage.layout();
        scrollMessage.setVvalue(1.0);
    }

    public void addMessageFrom(String date, String content) throws IOException{
        addMessage(date, content, "components/messageFrom.fxml");
    }

    public void addMessageTo(String date, String content) throws IOException{
        addMessage(date, content, "components/messageTo.fxml");
    }

    // Efface tous les messages de la discussion actuelle
    public void resetMessage(){
        messageContainer.getChildren().clear();
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

                if (GUI.currentDiscussionIndex >= 0){
                    String date = currentDate();
                    addMessageTo(date,messageText);
                    messageField.clear();

                    // A MODIFIER METTRE IP A LA PLACE
                    User user = GUI.connectedList.get(GUI.currentDiscussionIndex);
                    this.bdd.insertHistory(user.name, false, messageText, date);
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
            Boolean from = m.from;
            String content = m.content;
            String date = m.date;

            if (from){
                addMessageFrom(date,content);
            }
            else{
                addMessageTo(date,content);
            }
        }
    }

    public void addConnected(User user) throws IOException{
        FXMLLoader loader = new FXMLLoader();   
        HBox hbox = loader.load(getClass().getResource("components/connected.fxml").openStream());

        AnchorPane pane1 = (AnchorPane)hbox.getChildren().get(0);
        Label nameLabel = (Label) pane1.getChildren().get(0);
        nameLabel.setText(user.name);

        AnchorPane pane2 = (AnchorPane)hbox.getChildren().get(1);
        Label notificationLabel = (Label) pane2.getChildren().get(0);
        notificationLabel.setText("1");

        // Les éléments ne peuvent avoir comme ID que des strings (comme en html)
        hbox.setId(user.id.toString());
        hbox.setOnMouseClicked(e -> {
            try {
                updateCurrentDiscussion(e);
            } catch (SQLException | IOException e1) {
                e1.printStackTrace();
            }
        } );
        connectedContainer.getChildren().add(hbox);

        // TEMPORAIRE 
        // normalement on ajoute dans la liste de connecté de APP puis on update le "visuel"
        // ici on fait dans l'autre sens pour l'instant

        // Faudra faire attention au fait que si un utilisateur se connecte puis deconnecte
        // On sache qu'il était déjà dans la liste du GUI on le rajoute pas une deuxième fois
        GUI.connectedList.add(user);
    }

    public void removeConnected(Integer index){
        connectedContainer.getChildren().remove(index);
    }

    // Quand on clique sur la listeView cela choisi un user (surligné en bleu)
    // A chaque clique on regarde quel est l'utilisateur choisi
    // -> Load l'historique correspondant
    @FXML
    private void updateCurrentDiscussion(Event e) throws SQLException, IOException{

            HBox hbox = (HBox)e.getSource();
            Integer index = Integer.valueOf(hbox.getId());
            GUI.currentDiscussionIndex = index;

            resetMessage();

            // MODIFIER METTRE IP AU LIEU DE NOM
            // FAUT CORRESP IP/NOM
            User user = GUI.connectedList.get(index);
            ArrayList<Message> history = this.bdd.showHistory(user.name);

            loadMessages(history);

            System.out.println(user);
    }

    @FXML 
    private void clearHistory() throws SQLException{

        if (GUI.currentDiscussionIndex >= 0){
            User user = GUI.connectedList.get(GUI.currentDiscussionIndex);

            resetMessage();

            // MODIFIER METTRE IP AU LIEU DE NOM
            // FAUT CORRESP IP/NOM
            this.bdd.clearHistory(user.name);
        }
        else{
            alert.show();
        }
    }
}