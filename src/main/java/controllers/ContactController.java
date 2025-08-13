package controllers;

import entities.Contact;
import entities.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ContactService;
import services.HolidayService;
import utils.SceneManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ContactController implements Initializable {

    @FXML
    private TextField userEmailField;
    
    @FXML
    private TextField subjectField;
    
    @FXML
    private TextArea contentArea;
    
    @FXML
    private Button sendButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label messageLabel;
    
    @FXML
    private Label weekendNoticeLabel;
    
    @FXML
    private TextArea holidaysTextArea;

    private ContactService contactService;
    private HolidayService holidayService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contactService = new ContactService();
        holidayService = HolidayService.getInstance();
        
        // Set the email from the current user session
        if (UserSession.getInstance().getCurrentUser() != null) {
            userEmailField.setText(UserSession.getInstance().getCurrentUser().getEmail());
            userEmailField.setEditable(false); // Make it read-only
        }
        
        // Set the weekend notice
        weekendNoticeLabel.setText("Note: We do not process messages during weekends and holidays");
        weekendNoticeLabel.setStyle("-fx-text-fill: #666666; -fx-font-style: italic;");
        
        // Load and display holidays
        loadHolidays();
        
        // Clear message label initially
        messageLabel.setText("");
        
        setupEventHandlers();
    }

    private void loadHolidays() {
        try {
            List<String> holidays = holidayService.getHolidays();
            if (holidays.isEmpty()) {
                holidaysTextArea.setText("No upcoming holidays found.");
            } else {
                StringBuilder holidayText = new StringBuilder("Upcoming Holidays in Tunisia:\n\n");
                for (String holiday : holidays) {
                    holidayText.append("• ").append(holiday).append("\n");
                }
                holidaysTextArea.setText(holidayText.toString());
            }
            holidaysTextArea.setEditable(false);
            holidaysTextArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        } catch (Exception e) {
            holidaysTextArea.setText("Unable to load holiday information at this time.");
            holidaysTextArea.setEditable(false);
            System.err.println("Error loading holidays: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        sendButton.setOnAction(event -> handleSendMessage());
        backButton.setOnAction(event -> handleBackToDashboard());
    }

    @FXML
    private void handleSendMessage() {
        if (validateForm()) {
            try {
                Contact contact = new Contact(
                    userEmailField.getText().trim(),
                    subjectField.getText().trim(),
                    contentArea.getText().trim()
                );
                
                contactService.ajouter(contact);
                
                // Show success message
                showMessage("Message sent successfully! We will get back to you soon.", "success");
                
                // Clear form except email
                clearForm();
                
            } catch (Exception e) {
                showMessage("Error sending message. Please try again.", "error");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            // Récupérer le stage depuis n'importe quel élément de l'interface
            Stage stage = (Stage) backButton.getScene().getWindow();
            SceneManager.navigateToPage(stage, "/fxml/ClientDashboard.fxml", "Client Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (userEmailField.getText().trim().isEmpty()) {
            errors.append("Email is required.\n");
        }
        
        if (subjectField.getText().trim().isEmpty()) {
            errors.append("Subject is required.\n");
        }
        
        if (contentArea.getText().trim().isEmpty()) {
            errors.append("Message content is required.\n");
        }
        
        if (contentArea.getText().trim().length() > 1000) {
            errors.append("Message content must be less than 1000 characters.\n");
        }
        
        if (errors.length() > 0) {
            showMessage(errors.toString(), "error");
            return false;
        }
        
        return true;
    }

    private void clearForm() {
        subjectField.clear();
        contentArea.clear();
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        if ("success".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else if ("error".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
    }
}
