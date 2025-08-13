package services;
import utils.PasswordHasher;
import utils.MyDatabase;
import utils.EmailConfig;
import java.sql.Connection;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.util.Properties;
import java.util.UUID;

public class restpasseService {
    private final Connection connection;

    // Configuration pour Gmail - utilise EmailConfig
    private static final String EMAIL_USERNAME = EmailConfig.EMAIL_USERNAME;
    private static final String EMAIL_PASSWORD = EmailConfig.EMAIL_PASSWORD;
    private static final String SMTP_HOST = EmailConfig.EMAIL_HOST;
    private static final String SMTP_PORT = EmailConfig.EMAIL_PORT;

    // Activer le débogage SMTP pour voir les détails de la connexion
    private static final boolean DEBUG_SMTP = true;

    // Pour une application JavaFX, nous utiliserons un format de token simple
    // que l'utilisateur pourra copier-coller dans l'application
    private static final String APP_NAME = EmailConfig.EMAIL_FROM_NAME;

    public restpasseService() {
        try {
            this.connection = MyDatabase.getInstance().getConnection();
            if (this.connection == null) {
                System.err.println("Erreur: Impossible d'obtenir une connexion à la base de données");
            } else {
                System.out.println("Connexion à la base de données établie avec succès");
                createTokenTableIfNotExists();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du service de réinitialisation de mot de passe: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible d'initialiser le service de réinitialisation de mot de passe", e);
        }
    }

    /**
     * Crée la table des tokens de réinitialisation si elle n'exi-ste pas
     */
    private void createTokenTableIfNotExists() {
        if (connection == null) {
            System.err.println("Erreur: La connexion à la base de données est null");
            return;
        }

        String query = "CREATE TABLE IF NOT EXISTS password_reset_tokens (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "email VARCHAR(255) NOT NULL, " +
                "token VARCHAR(255) NOT NULL, " +
                "expiry_date TIMESTAMP NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_token (token)" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
            System.out.println("Table password_reset_tokens vérifiée/créée avec succès");

            // Vérifier si la table a bien été créée
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'password_reset_tokens'")) {
                if (rs.next()) {
                    System.out.println("Table password_reset_tokens existe");
                } else {
                    System.err.println("Erreur: La table password_reset_tokens n'a pas été créée");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table password_reset_tokens: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Demande une réinitialisation de mot de passe
     * @param email Email de l'utilisateur
     * @return true si la demande a été traitée avec succès, false sinon
     */
    public boolean requestPasswordReset(String email) {
        // Vérifier si l'email existe
        if (!emailExists(email)) {
            System.out.println("Email non trouvé: " + email);
            return false;
        }

        // Générer un token unique
        String token = generateToken();

        // Enregistrer le token dans la base de données
        if (!saveToken(email, token)) {
            return false;
        }

        // Envoyer un email avec le lien de réinitialisation
        return sendResetEmail(email, token);
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur
     * @param token Token de réinitialisation
     * @param newPassword Nouveau mot de passe
     * @return true si la réinitialisation a réussi, false sinon
     */
    public boolean resetPassword(String token, String newPassword) {
        // Vérifier si le token est valide et non expiré
        String email = getEmailFromToken(token);
        if (email == null) {
            System.out.println("Token invalide ou expiré: " + token);
            return false;
        }

        // Hacher le nouveau mot de passe avec notre PasswordHasher au format $2y$
        String hashedPassword = PasswordHasher.hashPassword(newPassword);

        // Mettre à jour le mot de passe dans la base de données
        if (!updatePassword(email, hashedPassword)) {
            return false;
        }

        // Invalider le token
        return invalidateToken(token);
    }

    /**
     * Génère un token unique
     * @return Token généré
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Enregistre un token dans la base de données
     * @param email Email de l'utilisateur
     * @param token Token généré
     * @return true si l'enregistrement a réussi, false sinon
     */
    private boolean saveToken(String email, String token) {
        // D'abord, supprimer les anciens tokens pour cet email
        String deleteQuery = "DELETE FROM password_reset_tokens WHERE email = ?";
        try (PreparedStatement pst = connection.prepareStatement(deleteQuery)) {
            pst.setString(1, email);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression des anciens tokens: " + e.getMessage());
            // Continuer malgré l'erreur
        }

        // Ensuite, insérer le nouveau token
        String insertQuery = "INSERT INTO password_reset_tokens (email, token, expiry_date) VALUES (?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(insertQuery)) {
            pst.setString(1, email);
            pst.setString(2, token);
            // Expiration dans 24 heures
            pst.setTimestamp(3, new Timestamp(System.currentTimeMillis() + 86400000));

            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'enregistrement du token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie un email avec le code de réinitialisation
     * @param email Email de l'utilisateur
     * @param token Token de réinitialisation
     * @return true si l'envoi a réussi, false sinon
     */
    private boolean sendResetEmail(String email, String token) {
        try {
            // Configuration des propriétés pour l'envoi d'email
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");

            // Créer une session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });

            // Activer le débogage SMTP
            session.setDebug(DEBUG_SMTP);
            System.out.println("Débogage SMTP activé: " + DEBUG_SMTP);

            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME, APP_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Réinitialisation de mot de passe - " + APP_NAME);

            // Corps du message avec le token directement (pour une application de bureau)
            StringBuilder messageText = new StringBuilder();
            messageText.append("Bonjour,\n\n");
            messageText.append("Vous avez demandé une réinitialisation de mot de passe pour votre compte " + APP_NAME + ".\n\n");
            messageText.append("Voici votre code de réinitialisation :\n\n");
            messageText.append("=== " + token + " ===\n\n");
            messageText.append("Copiez ce code et collez-le dans l'application pour réinitialiser votre mot de passe.\n\n");
            messageText.append("Ce code expirera dans 24 heures.\n\n");
            messageText.append("Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.\n\n");
            messageText.append("Cordialement,\n");
            messageText.append("L'équipe de " + APP_NAME);

            message.setText(messageText.toString());

            // Envoyer le message
            Transport.send(message);

            System.out.println("Email de réinitialisation envoyé à " + email);
            return true;

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Erreur inattendue lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère l'email associé à un token
     * @param token Token de réinitialisation
     * @return Email associé au token, null si le token est invalide ou expiré
     */
    public String getEmailFromToken(String token) {
        String query = "SELECT email FROM password_reset_tokens WHERE token = ? AND expiry_date > ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, token);
            pst.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du token: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Met à jour le mot de passe d'un utilisateur
     * @param email Email de l'utilisateur
     * @param hashedPassword Mot de passe haché
     * @return true si la mise à jour a réussi, false sinon
     */
    private boolean updatePassword(String email, String hashedPassword) {
        String query = "UPDATE user SET password = ? WHERE email = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, hashedPassword);
            pst.setString(2, email);

            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Invalide un token après utilisation
     * @param token Token à invalider
     * @return true si l'invalidation a réussi, false sinon
     */
    private boolean invalidateToken(String token) {
        String query = "DELETE FROM password_reset_tokens WHERE token = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, token);

            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'invalidation du token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si un email existe dans la base de données
     * @param email Email à vérifier
     * @return true si l'email existe, false sinon
     */
    private boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
