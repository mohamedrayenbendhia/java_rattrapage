package controllers.Admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.AuthService;
import utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminSidebarController implements Initializable {

    @FXML
    private HBox dashboardHBox;

    @FXML
    private Button dashboardButton;

    @FXML
    private HBox userHBox;

    @FXML
    private Button userButton;

    @FXML
    private HBox profileHBox;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    private BorderPane mainBorderPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Stocker le loader dans les propriétés du noeud pour pouvoir y accéder depuis d'autres contrôleurs
        Node thisNode = dashboardButton.getParent().getParent().getParent();
        thisNode.getProperties().put("loader", thisNode.getProperties().get("javafx.fxml.FXMLLoader"));
    }

    public void setMainBorderPane(BorderPane mainBorderPane) {
        this.mainBorderPane = mainBorderPane;
    }

    @FXML
    void handleDashboardClick(ActionEvent event) {
        loadPage("AdminDashboard.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    private void handleUserListClick(ActionEvent event) {
        loadPage("UserList.fxml");
        setActiveButton(userButton);
    }

    @FXML
    private void handleProfileClick(ActionEvent event) {
        try {
            System.out.println("Loading profile page...");
            
            // Load profile page
            URL url = getClass().getResource("/fxml/profileuser.fxml");
            if (url == null) {
                System.err.println("FXML file not found: /fxml/profileuser.fxml");
                return;
            }

            System.out.println("Profile URL found: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Create new scene and set it on current stage (same as other navigation)
            Scene scene = dashboardButton.getScene();
            if (scene != null) {
                Stage stage = (Stage) scene.getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("User Profile");
                System.out.println("Profile page loaded successfully");
            } else {
                System.err.println("Scene is null");
            }
        } catch (IOException e) {
            System.err.println("Error loading profile page: " + e.getMessage());
            e.printStackTrace();
        }
        setActiveButton(profileButton);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // User logout
            AuthService authService = AuthService.getInstance();
            authService.logout();

            // Redirect to login page
            System.out.println("Logout successful");

            // Load login page
            URL url = getClass().getResource("/fxml/Login.fxml");
            if (url == null) {
                System.err.println("Unable to find FXML file: /fxml/Login.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            // Configure scene
            Scene scene = dashboardButton.getScene();
            if (scene != null) {
                Stage stage = (Stage) scene.getWindow();
                scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login");
                System.out.println("Redirected to login page");
            } else {
                System.err.println("Scene is null");
            }
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadPage(String fxmlPath) {
        try {
            System.out.println("Loading page: " + fxmlPath);

            // Build full path
            String fullPath = "/fxml/admin/" + fxmlPath;
            System.out.println("Full path: " + fullPath);

            // Get current stage
            Scene scene = dashboardButton.getScene();
            if (scene != null) {
                Stage stage = (Stage) scene.getWindow();
                
                // Use SceneManager to load with fixed size
                String title = "Admin Panel - " + fxmlPath.replace(".fxml", "");
                SceneManager.navigateToPage(stage, fullPath, title);
                
                System.out.println("Page loaded successfully with SceneManager");
            } else {
                System.err.println("Scene is null");
            }
        } catch (IOException e) {
            System.err.println("Error loading page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        // Reset all buttons
        dashboardButton.getStyleClass().remove("active");
        userButton.getStyleClass().remove("active");
        profileButton.getStyleClass().remove("active");

        // Set active button
        button.getStyleClass().add("active");
    }
}
