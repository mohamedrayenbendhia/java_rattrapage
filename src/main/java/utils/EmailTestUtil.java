package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Utilitaire pour tester la configuration email
 */
public class EmailTestUtil {
    
    public static void testEmailConfiguration() {
        System.out.println("=== Test de configuration email ===");
        System.out.println("Email: " + EmailConfig.EMAIL_USERNAME);
        System.out.println("Host: " + EmailConfig.EMAIL_HOST);
        System.out.println("Port: " + EmailConfig.EMAIL_PORT);
        System.out.println("Password length: " + (EmailConfig.EMAIL_PASSWORD != null ? EmailConfig.EMAIL_PASSWORD.length() : "null"));
        
        // Vérifier si le mot de passe semble être un vrai mot de passe d'application
        if (EmailConfig.EMAIL_PASSWORD == null || 
            EmailConfig.EMAIL_PASSWORD.equals("your-16-char-app-password") ||
            EmailConfig.EMAIL_PASSWORD.equals("REMPLACEZ-PAR-VOTRE-MOT-DE-PASSE-APP") ||
            EmailConfig.EMAIL_PASSWORD.contains("rvxs") ||
            EmailConfig.EMAIL_PASSWORD.length() < 10) {
            System.err.println("ERREUR: Le mot de passe d'application n'est pas configure correctement!");
            System.err.println("Vous devez :");
            System.err.println("1. Activer l'authentification a 2 facteurs sur votre compte Gmail");
            System.err.println("2. Generer un mot de passe d'application depuis https://myaccount.google.com/apppasswords");
            System.err.println("3. Remplacer EMAIL_PASSWORD dans EmailConfig.java par ce mot de passe");
            return;
        }
        
        if (EmailConfig.EMAIL_USERNAME == null || 
            EmailConfig.EMAIL_USERNAME.equals("your-email@gmail.com")) {
            System.err.println("ERREUR: L'adresse email n'est pas configuree correctement!");
            System.err.println("Remplacez EMAIL_USERNAME dans EmailConfig.java par votre vraie adresse Gmail");
            return;
        }
        
        System.out.println("Configuration de base OK, test de connexion...");
        testSmtpConnection();
    }
    
    private static void testSmtpConnection() {
        try {
            // Configuration des propriétés
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", EmailConfig.EMAIL_HOST);
            props.put("mail.smtp.port", EmailConfig.EMAIL_PORT);
            props.put("mail.smtp.ssl.trust", EmailConfig.EMAIL_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            
            // Créer une session avec authentification
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailConfig.EMAIL_USERNAME, EmailConfig.EMAIL_PASSWORD);
                }
            });
            
            // Activer le debug
            session.setDebug(true);
            
            // Tester la connexion
            Transport transport = session.getTransport("smtp");
            transport.connect(EmailConfig.EMAIL_HOST, Integer.parseInt(EmailConfig.EMAIL_PORT), 
                            EmailConfig.EMAIL_USERNAME, EmailConfig.EMAIL_PASSWORD);
            
            System.out.println("Connexion SMTP reussie !");
            transport.close();
            
            // Envoyer un email de test
            sendTestEmail(session);
            
        } catch (Exception e) {
            System.err.println("Erreur de connexion SMTP: " + e.getMessage());
            e.printStackTrace();
            
            // Suggestions d'erreurs communes
            if (e.getMessage().contains("535") || e.getMessage().contains("Authentication")) {
                System.err.println("\nSOLUTION SUGGEREE:");
                System.err.println("Cette erreur indique un probleme d'authentification.");
                System.err.println("1. Verifiez que votre mot de passe d'application est correct");
                System.err.println("2. Assurez-vous que l'authentification a 2 facteurs est activee");
                System.err.println("3. Generez un nouveau mot de passe d'application si necessaire");
            }
        }
    }
    
    private static void sendTestEmail(Session session) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailConfig.EMAIL_USERNAME, EmailConfig.EMAIL_FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EmailConfig.EMAIL_USERNAME));
            message.setSubject("Test Email Configuration - " + EmailConfig.EMAIL_FROM_NAME);
            message.setText("Ceci est un email de test pour verifier la configuration email.\n\n" +
                          "Si vous recevez cet email, la configuration fonctionne correctement !");
            
            Transport.send(message);
            System.out.println("Email de test envoye avec succes !");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        testEmailConfiguration();
    }
}
