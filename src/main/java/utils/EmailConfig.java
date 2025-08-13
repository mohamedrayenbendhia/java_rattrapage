package utils;

/**
 * Configuration centrale de l'envoi d'emails pour l'application.
 * Conserve l'utilisation de Gmail + mot de passe d'application.
 */
public class EmailConfig {
    // IMPORTANT: Remplacez ces valeurs par vos vraies informations Gmail
    public static final String EMAIL_USERNAME = "youssef.alaya40@gmail.com"; // Adresse Gmail expéditeur
    public static final String EMAIL_PASSWORD = "huoazuougsolzabq"; // NOUVEAU mot de passe d'application (16 caractères)
    public static final String EMAIL_HOST = "smtp.gmail.com";
    public static final String EMAIL_PORT = "587"; // STARTTLS
    public static final String EMAIL_FROM_NAME = "esprit"; // Nom visible par le destinataire
}
