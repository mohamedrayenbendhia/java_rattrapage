package entities;

/**
 * Classe singleton pour gérer la session utilisateur
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    // Constructeur privé pour empêcher l'instanciation directe
    private UserSession() {
    }

    /**
     * Obtient l'instance unique de UserSession
     * @return L'instance de UserSession
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Définit l'utilisateur actuellement connecté
     * @param user L'utilisateur connecté
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Obtient l'utilisateur actuellement connecté
     * @return L'utilisateur connecté
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur est connecté, false sinon
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
