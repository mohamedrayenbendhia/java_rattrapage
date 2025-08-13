package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    
    /**
     * Génère un hash de mot de passe compatible avec Symfony ($2y$ format)
     * @param password Le mot de passe en clair
     * @return Le hash du mot de passe au format $2y$
     */
    public static String hashPassword(String password) {
        // Générer un salt avec coût 13 comme Symfony
        String salt = BCrypt.gensalt(13);
        String hash = BCrypt.hashpw(password, salt);
        
        // Convertir $2a$ vers $2y$ pour compatibilité avec Symfony
        if (hash.startsWith("$2a$")) {
            hash = hash.replaceFirst("\\$2a\\$", "\\$2y\\$");
        }
        
        return hash;
    }
    
    /**
     * Vérifie un mot de passe contre un hash (compatible avec $2y$ et $2a$)
     * @param password Le mot de passe en clair
     * @param hash Le hash stocké en base
     * @return true si le mot de passe correspond
     */
    public static boolean verifyPassword(String password, String hash) {
        // Vérifier que les paramètres ne sont pas null
        if (password == null || hash == null) {
            return false;
        }
        
        // Convertir $2y$ vers $2a$ pour la vérification avec BCrypt Java
        String hashForVerification = hash;
        if (hash.startsWith("$2y$")) {
            hashForVerification = hash.replaceFirst("\\$2y\\$", "\\$2a\\$");
        }
        
        return BCrypt.checkpw(password, hashForVerification);
    }
    
    public static void main(String[] args) {
        String password = "SuperAdmin2024!";
        String hashedPassword = hashPassword(password);
        System.out.println("Mot de passe: " + password);
        System.out.println("Hash BCrypt (format Symfony $2y$): " + hashedPassword);
        
        // Test de vérification
        boolean isValid = verifyPassword(password, hashedPassword);
        System.out.println("Vérification: " + isValid);
        
        // Test avec un hash Symfony existant
        String symfonyHash = "$2y$13$5MCKKX4Nz6uecDQae0sAD.oNzR3tK9vgo/9ZsDZpEEih7VnsXSmqi";
        System.out.println("Test avec hash Symfony: " + verifyPassword("motdepasse", symfonyHash));
    }
}
