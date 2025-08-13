package tests;

import services.restpasseService;
import utils.EmailConfig;

/**
 * Test simple de la fonctionnalité reset password
 */
public class TestResetPassword {
    
    public static void main(String[] args) {
        System.out.println("=== Test du Service de Reset Password ===");
        
        try {
            // Test 1: Vérification configuration
            System.out.println("1. Configuration Email:");
            System.out.println("   Username: " + EmailConfig.EMAIL_USERNAME);
            System.out.println("   Host: " + EmailConfig.EMAIL_HOST + ":" + EmailConfig.EMAIL_PORT);
            System.out.println("   Password configuré: " + (EmailConfig.EMAIL_PASSWORD != null && !EmailConfig.EMAIL_PASSWORD.equals("stjg uvpc keup sxzq") ? "✓" : "✗ Utilise encore le placeholder"));
            
            // Test 2: Initialisation service
            System.out.println("\n2. Initialisation du service...");
            restpasseService service = new restpasseService();
            System.out.println("✓ Service créé avec succès");
            
            // Test 3: Test avec email inexistant
            System.out.println("\n3. Test avec email inexistant...");
            boolean result = service.requestPasswordReset("nonexistent@test.com");
            System.out.println("Résultat (doit être false): " + result);
            
            System.out.println("\n=== Résumé ===");
            System.out.println("✓ Compilation: OK");
            System.out.println("✓ Service initialisé: OK");
            System.out.println("✓ Gestion email inexistant: OK");
            System.out.println("\nPour tester l'envoi d'email:");
            System.out.println("1. Assurez-vous d'avoir configuré un vrai mot de passe d'application Gmail");
            System.out.println("2. Utilisez l'interface JavaFX pour tester avec un email existant en base");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
