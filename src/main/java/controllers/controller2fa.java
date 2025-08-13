package controllers;

import entities.User;
import entities.UserSession;
import services.AuthService;
import services.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;

public class controller2fa {

    @FXML private ImageView imageView;
    @FXML private TextField codeField;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label secretKeyLabel;

    private String secretKey;
    private User currentUser;
    private String userEmail; // Variable pour stocker l'email

    @FXML
    public void initialize() {
        // Cette méthode est appelée automatiquement par JavaFX
        // Ne rien faire ici, car nous attendons que l'email soit défini via setEmail
        System.out.println("Controller2fa initialisé, en attente de l'email...");
    }

    public void setEmail(String email) {
        this.userEmail = email;
        System.out.println("Email reçu dans setEmail: " + email);

        // Récupérer l'utilisateur par son email
        try {
            AuthService authService = AuthService.getInstance();
            User user = authService.getUserByEmail(email);
            if (user != null) {
                this.currentUser = user;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
        }

        initialize2FA();
    }

    private void initialize2FA() {
        try {
            // Vérifier si l'email est défini
            if (userEmail == null || userEmail.isEmpty()) {
                System.err.println("Erreur: Email non défini dans initialize2FA");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Email manquant", "L'email n'a pas été correctement transmis.");
                return;
            }

            // Afficher les informations de l'utilisateur dans l'interface
            if (userEmailLabel != null) {
                userEmailLabel.setText(userEmail);
            }

            if (userNameLabel != null && currentUser != null) {
                userNameLabel.setText(currentUser.getName());
            }

            System.out.println("Email reçu pour 2FA: " + userEmail);

            // Générer une clé secrète aléatoire
            secretKey = Base32.random();

            // Afficher la clé secrète dans l'interface
            if (secretKeyLabel != null) {
                secretKeyLabel.setText(secretKey);
            }

            // Construire l'URL OTP avec l'email
            String issuer = "JavaFXApp";
            String accountName = userEmail;
            String otpUrl = String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    issuer, accountName, secretKey, issuer
            );

            System.out.println("OTP URL: " + otpUrl);

            generateQRCode(otpUrl);

        } catch (Exception e) {
            System.err.println("Exception dans initialize2FA: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'initialisation 2FA", e.getMessage());
        }
    }

    private void generateQRCode(String otpUrl) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix matrix = qrCodeWriter.encode(otpUrl, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        Image fxImage = SwingFXUtils.toFXImage(image, null);
        imageView.setImage(fxImage);
    }

    @FXML
    public void handleValidate2FA() {
        try {
            // Vérifier si l'email et la clé secrète sont définis
            if (userEmail == null || userEmail.isEmpty()) {
                System.err.println("Erreur: Email non défini dans handleValidate2FA");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Email manquant",
                        "L'email n'a pas été correctement initialisé.");
                return;
            }

            if (secretKey == null || secretKey.isEmpty()) {
                System.err.println("Erreur: Clé secrète non définie dans handleValidate2FA");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Clé secrète manquante",
                        "La clé secrète n'a pas été correctement générée.");
                return;
            }

            // Vérifier si le champ de code est vide
            String codeText = codeField.getText();
            if (codeText == null || codeText.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Code manquant",
                        "Veuillez entrer le code à 6 chiffres de votre application d'authentification.");
                return;
            }

            // Valider le code entré par l'utilisateur
            System.out.println("Tentative de validation du code: " + codeText + " avec la clé secrète: " + secretKey);

            Totp totp = new Totp(secretKey);
            boolean isCodeValid = totp.verify(codeText);
            System.out.println("Résultat de la validation du code: " + (isCodeValid ? "Valide" : "Invalide"));

            if (isCodeValid) {
                // Appel au service pour mettre à jour is_verified + enregistrer la clé
                System.out.println("Code valide, tentative d'enregistrement dans la base de données...");
                AuthService authService = AuthService.getInstance();
                boolean updated = authService.updateUserSecret(userEmail, secretKey);

                if (updated) {
                    System.out.println("Mise à jour réussie dans la base de données");
                    showAlert(Alert.AlertType.INFORMATION,
                            "Succès",
                            null,
                            "✅ Code valide ! Votre compte a été vérifié et l'authentification à deux facteurs a été activée.");
                    // Rediriger vers la page de connexion
                    // ...
                    goToLogin();
                } else {
                    System.err.println("Échec de la mise à jour dans la base de données");
                    showAlert(Alert.AlertType.ERROR,
                            "Erreur",
                            null,
                            "Le code est valide mais l'enregistrement en base a échoué. Vérifiez les logs pour plus de détails.");
                }
            } else {
                System.out.println("Code invalide: " + codeText);
                showAlert(Alert.AlertType.WARNING,
                        "Échec de la validation",
                        null,
                        "❌ Code invalide. Réessayez.");
            }

        } catch (NumberFormatException e) {
            System.err.println("Erreur de format de nombre: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format invalide", "Veuillez entrer un code numérique à 6 chiffres.");
        } catch (Exception e) {
            System.err.println("Exception dans handleValidate2FA: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la validation", "Une erreur inattendue s'est produite: " + e.getMessage());
        }


    }
    private void goToLogin() {
        try {
            // Utiliser le bon chemin pour le fichier Login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Connexion");
            stage.setScene(new Scene(root));
            stage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) imageView.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page de connexion : " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'accéder à la page de connexion", e.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}

