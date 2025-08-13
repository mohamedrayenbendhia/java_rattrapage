package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import services.restpasseService;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Contrôleur pour la réinitialisation de mot de passe
 */
public class ResetPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button requestResetButton;

    @FXML
    private Button verifyCodeButton;

    @FXML
    private Button resetPasswordButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label instructionsLabel;

    @FXML
    private VBox emailStep;

    @FXML
    private VBox codeStep;

    @FXML
    private VBox passwordStep;

    private restpasseService resetService;
    private String currentEmail;
    private String resetToken;

    /**
     * Initialise le contrôleur
     */
    @FXML
    public void initialize() {
        resetService = new restpasseService();

        // Afficher uniquement la première étape au démarrage
        showEmailStep();

        // Action pour le lien de connexion
        loginLink.setOnAction(event -> navigateToLogin());
    }

    /**
     * Affiche l'étape de saisie de l'email
     */
    private void showEmailStep() {
        emailStep.setVisible(true);
        codeStep.setVisible(false);
        passwordStep.setVisible(false);
        instructionsLabel.setText("Entrez votre adresse email pour réinitialiser votre mot de passe.");
    }

    /**
     * Affiche l'étape de saisie du code
     */
    private void showCodeStep() {
        emailStep.setVisible(false);
        codeStep.setVisible(true);
        passwordStep.setVisible(false);
        instructionsLabel.setText("Un code de vérification a été envoyé à votre adresse email. Veuillez l'entrer ci-dessous.");
    }

    /**
     * Affiche l'étape de saisie du nouveau mot de passe
     */
    private void showPasswordStep() {
        emailStep.setVisible(false);
        codeStep.setVisible(false);
        passwordStep.setVisible(true);
        instructionsLabel.setText("Entrez votre nouveau mot de passe.");
    }

    /**
     * Gère la demande de réinitialisation de mot de passe
     */
    @FXML
    private void handleRequestReset(ActionEvent event) {
        String email = emailField.getText().trim();

        // Vérifier que l'email n'est pas vide
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer votre adresse email.");
            return;
        }

        // Demander une réinitialisation de mot de passe
        // Cette méthode génère un token, l'enregistre et envoie l'email
        instructionsLabel.setText("Envoi du code de réinitialisation en cours...");

        // Créer une tâche en arrière-plan pour la demande de réinitialisation
        Task<Boolean> resetTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Demander une réinitialisation de mot de passe
                return resetService.requestPasswordReset(email);
            }
        };

            // Gérer le résultat de la tâche
            resetTask.setOnSucceeded(e -> {
                Boolean resetRequested = resetTask.getValue();
                if (resetRequested) {
                    // Demande de réinitialisation réussie
                    showAlert(Alert.AlertType.INFORMATION, "Code envoyé",
                            "Un code de réinitialisation a été envoyé à votre adresse email.");

                    // Stocker l'email pour les étapes suivantes
                    currentEmail = email;

                    // Passer à l'étape suivante
                    showCodeStep();
                } else {
                    // Échec de la demande de réinitialisation
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de traiter votre demande. Veuillez vérifier votre email et réessayer plus tard.");
                }
            });

            resetTask.setOnFailed(e -> {
                Throwable exception = resetTask.getException();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la demande de réinitialisation: " + exception.getMessage());
                exception.printStackTrace();
            });

            // Démarrer la tâche
            new Thread(resetTask).start();
    }

    /**
     * Gère la vérification du code
     */
    @FXML
    private void handleVerifyCode(ActionEvent event) {
        String code = codeField.getText().trim();

        // Vérifier que le code n'est pas vide
        if (code.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer le code de vérification.");
            return;
        }

        // Vérifier si le token est valide
        String email = resetService.getEmailFromToken(code);
        if (email != null) {
            // Token valide, passer à l'étape suivante
            currentEmail = email; // Mettre à jour l'email avec celui récupéré du token
            resetToken = code; // Stocker le token pour la réinitialisation du mot de passe
            showPasswordStep();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Code invalide ou expiré. Veuillez réessayer.");
        }
    }

    /**
     * Gère la réinitialisation du mot de passe
     */
    @FXML
    private void handleResetPassword(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Vérifier que les champs ne sont pas vides
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        // Vérifier que les mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Les mots de passe ne correspondent pas.");
            return;
        }

        // Réinitialiser le mot de passe avec le token
        boolean reset = resetService.resetPassword(resetToken, newPassword);

        if (reset) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre mot de passe a été réinitialisé avec succès.");

            // Rediriger vers la page de connexion
            navigateToLogin();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de réinitialiser le mot de passe. Veuillez réessayer.");
        }
    }

    /**
     * Navigue vers la page de connexion
     */
    private void navigateToLogin() {
        try {
            // Charger la page de connexion
            File file = new File("src/main/resources/fxml/Login.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configurer la scène
                Stage stage = (Stage) loginLink.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Erreur lors du chargement de la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
