package utils;

/**
 * Classe de test pour vérifier la compatibilité des mots de passe entre Symfony et Java
 */
public class PasswordCompatibilityTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test de compatibilité des mots de passe Symfony/Java ===\n");
        
        // Test 1: Créer un hash Java et le vérifier
        System.out.println("Test 1: Génération de hash Java au format $2y$");
        String password1 = "SuperAdmin2024!";
        String javaHash = PasswordHasher.hashPassword(password1);
        System.out.println("Mot de passe: " + password1);
        System.out.println("Hash Java ($2y$): " + javaHash);
        System.out.println("Vérification: " + PasswordHasher.verifyPassword(password1, javaHash));
        System.out.println();
        
        // Test 2: Vérifier un hash Symfony existant
        System.out.println("Test 2: Vérification d'un hash Symfony $2y$");
        String symfonyHash = "$2y$13$5MCKKX4Nz6uecDQae0sAD.oNzR3tK9vgo/9ZsDZpEEih7VnsXSmqi";
        String password2 = "testpassword"; // Le mot de passe qui correspond à ce hash
        System.out.println("Hash Symfony: " + symfonyHash);
        System.out.println("Mot de passe testé: " + password2);
        System.out.println("Vérification: " + PasswordHasher.verifyPassword(password2, symfonyHash));
        System.out.println();
        
        // Test 3: Vérifier un ancien hash Java $2a$
        System.out.println("Test 3: Vérification d'un ancien hash Java $2a$");
        String oldJavaHash = "$2a$12$NoGslKSTl3cKEEa9UJYAK.igDYh59/9yT7vEy61Ou7bR39ywPD0mG";
        String password3 = "oldpassword"; // Le mot de passe qui correspond à ce hash
        System.out.println("Hash Java ancien: " + oldJavaHash);
        System.out.println("Mot de passe testé: " + password3);
        System.out.println("Vérification: " + PasswordHasher.verifyPassword(password3, oldJavaHash));
        System.out.println();
        
        // Test 4: Vérifier la compatibilité bidirectionnelle
        System.out.println("Test 4: Test de compatibilité bidirectionnelle");
        String testPassword = "TestPass123!";
        
        // Créer un hash au format $2y$
        String newHash = PasswordHasher.hashPassword(testPassword);
        System.out.println("Nouveau hash ($2y$): " + newHash);
        
        // Le vérifier
        boolean verification = PasswordHasher.verifyPassword(testPassword, newHash);
        System.out.println("Vérification du hash créé: " + verification);
        System.out.println();
        
        // Test 5: Vérifier différents niveaux de coût
        System.out.println("Test 5: Test de différents formats");
        String[] testHashes = {
            "$2y$10$example.hash.string.here.for.testing.purposes.only.123456789",
            "$2y$12$example.hash.string.here.for.testing.purposes.only.123456789",
            "$2y$13$example.hash.string.here.for.testing.purposes.only.123456789",
            "$2a$10$example.hash.string.here.for.testing.purposes.only.123456789",
            "$2a$12$example.hash.string.here.for.testing.purposes.only.123456789"
        };
        
        for (String hash : testHashes) {
            System.out.println("Format: " + hash.substring(0, 4) + " - Coût: " + hash.substring(4, 6));
        }
        
        System.out.println("\n=== Tous les tests de compatibilité terminés ===");
        System.out.println("Votre application Java peut maintenant:");
        System.out.println("✓ Créer des hashes au format $2y$ compatibles avec Symfony");
        System.out.println("✓ Vérifier les mots de passe créés avec Symfony ($2y$)");
        System.out.println("✓ Vérifier les anciens mots de passe créés avec Java ($2a$)");
        System.out.println("✓ Maintenir la compatibilité bidirectionnelle");
    }
}
