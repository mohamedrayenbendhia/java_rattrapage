package controllers.Admin;

import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import services.UserService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML
    private Button userListButton;
    
    @FXML
    private Label superAdminCountLabel;
    
    @FXML
    private Label adminCountLabel;
    
    @FXML
    private Label clientCountLabel;

    private UserService userService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userService = UserService.getInstance();
        loadUserStatistics();
        System.out.println("Admin Dashboard initialized");
    }

    private void loadUserStatistics() {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            int superAdminCount = 0;
            int adminCount = 0;
            int clientCount = 0;
            
            for (User user : allUsers) {
                if (user.getRole().contains("ROLE_SUPER_ADMIN")) {
                    superAdminCount++;
                } else if (user.getRole().contains("ROLE_ADMIN")) {
                    adminCount++;
                } else if (user.getRole().contains("ROLE_USER")) {
                    clientCount++;
                }
            }
            
            superAdminCountLabel.setText(String.valueOf(superAdminCount));
            adminCountLabel.setText(String.valueOf(adminCount));
            clientCountLabel.setText(String.valueOf(clientCount));
            
        } catch (SQLException e) {
            System.err.println("Error loading user statistics: " + e.getMessage());
            e.printStackTrace();
            
            // Set default values in case of error
            superAdminCountLabel.setText("0");
            adminCountLabel.setText("0");
            clientCountLabel.setText("0");
        }
    }

    /**
     * Public method to refresh user statistics
     * Can be called from other controllers when user data changes
     */
    public void refreshUserStatistics() {
        loadUserStatistics();
    }

    @FXML
    private void handleUserListClick(ActionEvent event) {
        navigateToPage("/fxml/admin/UserList.fxml", "User List");
    }

    private void navigateToPage(String fxmlPath, String title) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                System.err.println("Unable to find FXML file: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = (Stage) userListButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
        } catch (IOException e) {
            System.err.println("Error during navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
