package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Utilitaire pour tester rapidement la configuration Gmail
 */
public class GmailConfigTester {
    
    public static void main(String[] args) {
        testGmailConnection();
    }
    
    public static void testGmailConnection() {
        System.out.println("=== Test de Configuration Gmail ===");
        
        // Vérification de base
        System.out.println("Email: " + EmailConfig.EMAIL_USERNAME);
        System.out.println("Host: " + EmailConfig.EMAIL_HOST + ":" + EmailConfig.EMAIL_PORT);
        System.out.println("Password configuré: " + (EmailConfig.EMAIL_PASSWORD != null ? "Oui (" + EmailConfig.EMAIL_PASSWORD.length() + " chars)" : "Non"));
        
        // Vérifier si le mot de passe semble valide
        if (EmailConfig.EMAIL_PASSWORD == null || 
            EmailConfig.EMAIL_PASSWORD.equals("REMPLACEZ-PAR-NOUVEAU-MOT-DE-PASSE") ||
            EmailConfig.EMAIL_PASSWORD.equals("stjg uvpc keup sxzq") ||
            EmailConfig.EMAIL_PASSWORD.length() < 16) {
            
            System.err.println("\n❌ PROBLÈME DÉTECTÉ:");
            System.err.println("Le mot de passe d'application n'est pas configuré correctement.");
            System.err.println("\nÉtapes à suivre:");
            System.err.println("1. Allez sur: https://myaccount.google.com/apppasswords");
            System.err.println("2. Connectez-vous avec: " + EmailConfig.EMAIL_USERNAME);
            System.err.println("3. Créez un nouveau mot de passe d'application");
            System.err.println("4. Remplacez EMAIL_PASSWORD dans EmailConfig.java");
            System.err.println("5. Relancez ce test");
            return;
        }
        
        // Test de connexion SMTP
        System.out.println("\n=== Test de Connexion SMTP ===");
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", EmailConfig.EMAIL_HOST);
            props.put("mail.smtp.port", EmailConfig.EMAIL_PORT);
            props.put("mail.smtp.ssl.trust", EmailConfig.EMAIL_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailConfig.EMAIL_USERNAME, EmailConfig.EMAIL_PASSWORD);
                }
            });
            
            // Test de connexion
            Transport transport = session.getTransport("smtp");
            transport.connect(EmailConfig.EMAIL_HOST, Integer.parseInt(EmailConfig.EMAIL_PORT), 
                            EmailConfig.EMAIL_USERNAME, EmailConfig.EMAIL_PASSWORD);
            
            System.out.println("✅ Connexion SMTP réussie!");
            transport.close();
            
            // Test d'envoi d'email
            sendTestEmail(session);
            
        } catch (MessagingException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            
            if (e.getMessage().contains("535")) {
                System.err.println("\n🔧 SOLUTION:");
                System.err.println("Erreur d'authentification - le mot de passe d'application est incorrect.");
                System.err.println("Générez un nouveau mot de passe sur: https://myaccount.google.com/apppasswords");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
        }
    }
    
    private static void sendTestEmail(Session session) {
        try {
            System.out.println("\n=== Test d'Envoi d'Email ===");
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailConfig.EMAIL_USERNAME, EmailConfig.EMAIL_FROM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EmailConfig.EMAIL_USERNAME));
            message.setSubject("Test Configuration Email - " + EmailConfig.EMAIL_FROM_NAME);
            
            String content = "Test réussi!\n\n" +
                           "Ce message confirme que la configuration email fonctionne correctement.\n" +
                           "Vous pouvez maintenant utiliser la fonctionnalité de reset password.\n\n" +
                           "Envoyé depuis votre application JavaFX.";
            
            message.setText(content);
            
            Transport.send(message);
            
            System.out.println("✅ Email de test envoyé avec succès!");
            System.out.println("Vérifiez votre boîte de réception: " + EmailConfig.EMAIL_USERNAME);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi: " + e.getMessage());
        }
    }
}
