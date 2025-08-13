package services;

import entities.User;
import entities.UserSession;
import services.RoleService;
import utils.MyDatabase;
import utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Service pour gérer l'authentification des utilisateurs
 */
public class AuthService {
    private static AuthService instance;
    private final Connection connection;

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private AuthService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Authentifier un utilisateur avec son email et son mot de passe
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @return L'utilisateur authentifié ou null si l'authentification échoue
     * @throws SQLException En cas d'erreur SQL
     * @throws IllegalStateException Si le compte est bloqué
     */
    public User login(String email, String password) throws SQLException, IllegalStateException {
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String hashedPassword = resultSet.getString("password");
                    
                    // Vérifier le mot de passe avec notre PasswordHasher compatible $2y$/$2a$
                    if (PasswordHasher.verifyPassword(password, hashedPassword)) {
                        // Create User object with database data
                        User user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setName(resultSet.getString("name"));
                        user.setEmail(resultSet.getString("email"));
                        user.setPhone_number(resultSet.getString("phone_number"));
                        user.setVerified(resultSet.getBoolean("is_verified"));
                        user.setBlocked(resultSet.getBoolean("is_blocked"));
                        user.setCreated_at(resultSet.getTimestamp("created_at"));
                        user.setPassword(hashedPassword); // Important : assigner le mot de passe haché
                        user.setImage(resultSet.getString("image"));
                        user.setSecretKey(resultSet.getString("secret_key")); // Get 2FA secret key

                        // Récupérer les rôles depuis le champ JSON
                        String rolesJson = resultSet.getString("roles");
                        user.setRole(parseRolesFromJson(rolesJson));

                        // Check if account is blocked
                        if (user.isBlocked()) {
                            throw new IllegalStateException("Your account is blocked. Please contact administrator.");
                        }

                        // Store user in session
                        UserSession.getInstance().setCurrentUser(user);

                        return user;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Register a new user
     * @param user The user to register
     * @throws SQLException In case of SQL error
     */
    public void register(User user) throws SQLException {
        // Assigner le rôle par défaut "ROLE_USER" 
        user.getRole().clear();
        user.getRole().add("ROLE_USER");
        
        // Hasher le mot de passe avec notre PasswordHasher au format $2y$
        String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
        
        // Convertir la liste des rôles en JSON
        String rolesJson = "[\"ROLE_USER\"]";
        
        String query = "INSERT INTO user (name, email, phone_number, password, is_verified, created_at, roles, is_blocked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone_number());
            statement.setString(4, hashedPassword); // Utiliser le mot de passe hashé
            statement.setBoolean(5, user.isVerified());
            statement.setTimestamp(6, user.getCreated_at());
            statement.setString(7, rolesJson);
            statement.setBoolean(8, user.isBlocked()); // Utiliser la valeur de l'entité

            statement.executeUpdate();
        }
    }
    
    /**
     * Récupérer un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // Create User object with database data
                    User user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setName(resultSet.getString("name"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPhone_number(resultSet.getString("phone_number"));
                    user.setVerified(resultSet.getBoolean("is_verified"));
                    user.setBlocked(resultSet.getBoolean("is_blocked"));
                    user.setCreated_at(resultSet.getTimestamp("created_at"));
                    user.setPassword(resultSet.getString("password")); // Important : récupérer le mot de passe
                    user.setImage(resultSet.getString("image"));

                    // Récupérer les rôles depuis le champ JSON
                    String rolesJson = resultSet.getString("roles");
                    user.setRole(parseRolesFromJson(rolesJson));

                    return user;
                }
            }
        }

        return null;
    }

    /**
     * Mettre à jour la clé secrète 2FA d'un utilisateur et marquer son compte comme vérifié
     * @param email L'email de l'utilisateur
     * @param secretKey La clé secrète 2FA
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateUserSecret(String email, String secretKey) {
        String query = "UPDATE user SET secret_key = ?, is_verified = true WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, secretKey);
            statement.setString(2, email);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Utilisateur " + email + " vérifié avec succès (is_verified = true).");
                return true;
            } else {
                System.err.println("Aucune ligne affectée lors de la mise à jour de l'utilisateur " + email);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la clé secrète : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier si un email existe déjà dans la base de données
     * @param email L'email à vérifier
     * @return true si l'email existe, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Check if a phone number already exists in the database
     * @param phoneNumber The phone number to check
     * @return true if the phone number exists, false otherwise
     * @throws SQLException In case of SQL error
     */
    public boolean phoneExists(String phoneNumber) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE phone_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, phoneNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Déconnecter l'utilisateur actuel
     */
    public void logout() {
        UserSession.getInstance().logout();
    }

    /**
     * Obtenir l'utilisateur actuellement connecté
     * @return L'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public User getCurrentUser() {
        return UserSession.getInstance().getCurrentUser();
    }
    
    /**
     * Parser les rôles depuis une chaîne JSON
     * @param rolesJson La chaîne JSON contenant les rôles
     * @return Liste des rôles
     */
    private List<String> parseRolesFromJson(String rolesJson) {
        List<String> roles = new ArrayList<>();
        
        if (rolesJson != null && !rolesJson.isEmpty()) {
            try {
                // Enlever les crochets et guillemets
                rolesJson = rolesJson.trim();
                if (rolesJson.startsWith("[") && rolesJson.endsWith("]")) {
                    rolesJson = rolesJson.substring(1, rolesJson.length() - 1);
                }
                
                // Séparer les rôles par virgule
                String[] roleArray = rolesJson.split(",");
                for (String role : roleArray) {
                    role = role.trim();
                    // Enlever les guillemets
                    if (role.startsWith("\"") && role.endsWith("\"")) {
                        role = role.substring(1, role.length() - 1);
                    }
                    if (!role.isEmpty()) {
                        roles.add(role);
                    }
                }
            } catch (Exception e) {
                // En cas d'erreur, assigner le rôle par défaut
                roles.add("ROLE_USER");
            }
        } else {
            // Si pas de rôles, assigner le rôle par défaut
            roles.add("ROLE_USER");
        }
        
        return roles;
    }

    /**
     * Logout the current user by clearing the session
     */
    
}
