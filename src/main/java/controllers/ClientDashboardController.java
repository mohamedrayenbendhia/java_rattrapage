package controllers;

import entities.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.AuthService;
import services.RoleService;
import utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {

    // Instance statique pour permettre le rafraîchissement des statistiques depuis d'autres contrôleurs
    private static ClientDashboardController instance;

    @FXML
    private Text userInfoText;

    @FXML
    private Text totalUsersText;

    @FXML
    private Text connectedUsersText;

    @FXML
    private Text nameText;

    @FXML
    private Text firstNameText;

    @FXML
    private Text emailText;

    @FXML
    private Text addressText;

    @FXML
    private Text phoneText;

    private AuthService authService;
    private RoleService roleService;

    public ClientDashboardController() {
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Stocker l'instance pour permettre le rafraîchissement des statistiques
        instance = this;

        // Display logged in user information
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            userInfoText.setText("Connected as: " + currentUser.getName());

            // Fill profile information
            nameText.setText(currentUser.getName());
            firstNameText.setText(""); // No longer using first name
            emailText.setText(currentUser.getEmail());
            addressText.setText(""); // No longer using address
            phoneText.setText(currentUser.getPhone_number());
        }

        // Charger les statistiques
        loadStatistics();
    }

    /**
     * Charge les statistiques utilisateur
     * Méthode privée utilisée en interne
     */
    private void loadStatistics() {
        try {
            // Statistiques simples pour les utilisateurs
            // Vous pouvez ajouter ici des statistiques comme le nombre total d'utilisateurs inscrits
            totalUsersText.setText("0"); // À implémenter selon vos besoins
            connectedUsersText.setText("1"); // Utilisateur actuel connecté
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchit les statistiques
     * Méthode publique pouvant être appelée depuis d'autres contrôleurs
     */
    public void refreshStatistics() {
        loadStatistics();
    }

    /**
     * Méthode statique pour rafraîchir les statistiques depuis n'importe quel contrôleur
     */
    public static void refreshDashboardStatistics() {
        if (instance != null) {
            instance.refreshStatistics();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Déconnecter l'utilisateur
        authService.logout();

        try {
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) userInfoText.getScene().getWindow();
            
            // Utiliser SceneManager pour naviguer avec une taille fixe
            SceneManager.navigateToPage(stage, "/fxml/Login.fxml", "Connexion");
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page de connexion.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExit(ActionEvent event) {
        // Quitter l'application
        Platform.exit();
    }

    @FXML
    public void handleViewProfile(ActionEvent event) {
        try {
            // Obtenir la fenêtre actuelle à partir de l'événement
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // Utiliser SceneManager pour naviguer avec une taille fixe
            SceneManager.navigateToPage(stage, "/fxml/profileuser.fxml", "Profil Utilisateur");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la vue du profil.");
        }
    }

    @FXML
    public void handleEditProfile(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité non implémentée");
    }

    @FXML
    public void handleSearchEvents(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité de recherche d'événements non implémentée");
    }

    @FXML
    public void handleMyReservations(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Information", "Fonctionnalité de mes réservations non implémentée");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
