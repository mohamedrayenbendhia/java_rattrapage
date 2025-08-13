package utils;

import services.restpasseService;

/**
 * Test rapide de la fonctionnalité reset password
 */
public class ResetPasswordTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test du Service de Reset Password ===");
        
        try {
            // Test 1: Initialisation du service
            System.out.println("1. Initialisation du service...");
            restpasseService service = new restpasseService();
            System.out.println("✓ Service initialisé avec succès");
            
            // Test 2: Test de configuration email
            System.out.println("\n2. Vérification de la configuration email...");
            System.out.println("Email configuré: " + EmailConfig.EMAIL_USERNAME);
            System.out.println("Host: " + EmailConfig.EMAIL_HOST + ":" + EmailConfig.EMAIL_PORT);
            System.out.println("Mot de passe configuré: " + (EmailConfig.EMAIL_PASSWORD != null && EmailConfig.EMAIL_PASSWORD.length() > 10 ? "✓ Oui" : "✗ Non"));
            
            // Test 3: Test avec un email non existant (doit échouer proprement)
            System.out.println("\n3. Test avec email inexistant...");
            boolean result1 = service.requestPasswordReset("test-inexistant@example.com");
            System.out.println("Résultat attendu (false): " + result1);
            
            // Test 4: Test avec l'email configuré (doit réussir si config OK)
            System.out.println("\n4. Test avec email configuré...");
            // Note: Décommentez cette ligne seulement si vous voulez vraiment envoyer un email
            // boolean result2 = service.requestPasswordReset(EmailConfig.EMAIL_USERNAME);
            // System.out.println("Résultat envoi email: " + result2);
            System.out.println("(Test d'envoi d'email désactivé pour éviter spam)");
            
            System.out.println("\n=== Tests terminés ===");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
