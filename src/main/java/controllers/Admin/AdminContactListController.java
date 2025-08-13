package controllers.Admin;

import entities.Contact;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import services.ContactService;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminContactListController implements Initializable {

    @FXML
    private ListView<Contact> contactListView;
    
    @FXML
    private Label messageCountLabel;

    private ContactService contactService;
    private ObservableList<Contact> contactList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contactService = new ContactService();
        contactList = FXCollections.observableArrayList();
        
        setupListView();
        loadContacts();
    }

    private void setupListView() {
        contactListView.setItems(contactList);
        
        // Custom cell factory to display contact information
        contactListView.setCellFactory(listView -> new ListCell<Contact>() {
            @Override
            protected void updateItem(Contact contact, boolean empty) {
                super.updateItem(contact, empty);
                
                if (empty || contact == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox contactCard = createContactCard(contact);
                    setGraphic(contactCard);
                    setText(null);
                }
            }
        });
    }

    private VBox createContactCard(Contact contact) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-border-width: 1;");
        
        // Header with email and date
        VBox header = new VBox(3);
        
        Text emailText = new Text("From: " + contact.getUserEmail());
        emailText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-fill: #2c3e50;");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        Text dateText = new Text("Sent: " + contact.getCreatedAt().format(formatter));
        dateText.setStyle("-fx-font-size: 12px; -fx-fill: #6c757d;");
        
        header.getChildren().addAll(emailText, dateText);
        
        // Subject
        Text subjectText = new Text("Subject: " + contact.getSubject());
        subjectText.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-fill: #495057;");
        
        // Content preview (first 100 characters)
        String contentPreview = contact.getContent().length() > 100 
            ? contact.getContent().substring(0, 100) + "..."
            : contact.getContent();
        
        Text contentText = new Text(contentPreview);
        contentText.setStyle("-fx-font-size: 12px; -fx-fill: #495057; -fx-wrap-text: true;");
        contentText.setWrappingWidth(500);
        
        card.getChildren().addAll(header, subjectText, contentText);
        
        return card;
    }

    private void loadContacts() {
        try {
            List<Contact> contacts = contactService.afficher();
            contactList.clear();
            contactList.addAll(contacts);
            
            // Update message count
            messageCountLabel.setText(String.valueOf(contacts.size()));
            
            System.out.println("Loaded " + contacts.size() + " contact messages");
            
        } catch (SQLException e) {
            System.err.println("Error loading contacts: " + e.getMessage());
            e.printStackTrace();
            messageCountLabel.setText("Error");
        }
    }

    @FXML
    private void handleRefresh() {
        loadContacts();
    }
}
