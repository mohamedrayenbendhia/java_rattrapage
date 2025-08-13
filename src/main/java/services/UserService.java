package services;

import entities.User;
import utils.MyDatabase;
import utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations liées aux utilisateurs
 */
public class UserService {
    private static UserService instance;
    private final Connection connection;

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private UserService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Récupérer un utilisateur par son ID
     * @param userId L'ID de l'utilisateur
     * @return L'utilisateur ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createUserFromResultSet(resultSet);
                }
            }
        }
        
        return null;
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
                    return createUserFromResultSet(resultSet);
                }
            }
        }
        
        return null;
    }

    /**
     * Update user information
     * @param user The user to update
     * @throws SQLException In case of SQL error
     */
    public void updateUser(User user) throws SQLException {
        String query = "UPDATE user SET name = ?, email = ?, phone_number = ?, image = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone_number());
            statement.setString(4, user.getImage());
            statement.setInt(5, user.getId());
            
            statement.executeUpdate();
        }
    }

    /**
     * Mettre à jour le mot de passe d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe (en clair)
     * @throws SQLException En cas d'erreur SQL
     */
    public void updatePassword(int userId, String newPassword) throws SQLException {
        // Hacher le mot de passe avec notre PasswordHasher au format $2y$
        String hashedPassword = PasswordHasher.hashPassword(newPassword);
        
        String query = "UPDATE user SET password = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, hashedPassword);
            statement.setInt(2, userId);
            
            statement.executeUpdate();
        }
    }

    /**
     * Vérifier si un email existe déjà (pour un autre utilisateur)
     * @param email L'email à vérifier
     * @param userId L'ID de l'utilisateur actuel (pour exclure de la vérification)
     * @return true si l'email existe pour un autre utilisateur, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean emailExistsForOtherUser(String email, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ? AND id != ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setInt(2, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }

    /**
     * Récupérer tous les utilisateurs
     * @return La liste de tous les utilisateurs
     * @throws SQLException En cas d'erreur SQL
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user ORDER BY name";
        
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                User user = createUserFromResultSet(resultSet);
                users.add(user);
            }
        }
        
        return users;
    }

    /**
     * Get all users excluding the current logged-in user and super admin
     * @param currentUserId The ID of the current logged-in user
     * @return List of users excluding current user and super admin
     * @throws SQLException In case of SQL error
     */
    public List<User> getAllUsersExcludingCurrentAndSuperAdmin(int currentUserId) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE id != ? ORDER BY name";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, currentUserId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = createUserFromResultSet(resultSet);
                    
                    // Exclude super admin users
                    if (!user.getRole().contains("ROLE_SUPER_ADMIN")) {
                        users.add(user);
                    }
                }
            }
        }
        
        return users;
    }

    /**
     * Block or unblock a user (only clients can be blocked)
     * @param userId The ID of the user to block/unblock
     * @param isBlocked The new blocked status
     * @throws SQLException In case of SQL error
     * @throws IllegalArgumentException If trying to block non-client user
     */
    public void updateUserBlockStatus(int userId, boolean isBlocked) throws SQLException, IllegalArgumentException {
        // First check if the user is a client (ROLE_USER)
        User user = getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Only clients (ROLE_USER) can be blocked
        if (!user.getRole().contains("ROLE_USER") || user.getRole().contains("ROLE_ADMIN") || user.getRole().contains("ROLE_SUPER_ADMIN")) {
            throw new IllegalArgumentException("Only client users can be blocked");
        }
        
        String query = "UPDATE user SET is_blocked = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, isBlocked);
            statement.setInt(2, userId);
            
            statement.executeUpdate();
        }
    }

    /**
     * Créer un objet User à partir d'un ResultSet
     * @param resultSet Le ResultSet contenant les données de l'utilisateur
     * @return L'objet User créé
     * @throws SQLException En cas d'erreur SQL
     */
    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone_number(resultSet.getString("phone_number"));
        user.setVerified(resultSet.getBoolean("is_verified"));
        user.setBlocked(resultSet.getBoolean("is_blocked"));
        user.setCreated_at(resultSet.getTimestamp("created_at"));
        user.setPassword(resultSet.getString("password"));
        user.setImage(resultSet.getString("image"));
        user.setSecretKey(resultSet.getString("secret_key"));
        
        // Récupérer les rôles depuis le champ JSON
        String rolesJson = resultSet.getString("roles");
        if (rolesJson != null && !rolesJson.isEmpty()) {
            user.setRole(parseRolesFromJson(rolesJson));
        } else {
            // Set default role for user
            user.getRole().add("ROLE_USER");
        }
        
        return user;
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
     * Add a new user with role restrictions
     * @param user The user to add
     * @param currentUserRole The role of the current user adding this user
     * @throws SQLException In case of SQL error
     * @throws IllegalArgumentException If role restrictions are violated
     */
    public void addUser(User user, String currentUserRole) throws SQLException, IllegalArgumentException {
        // Validate role restrictions
        if (user.getRole().contains("ROLE_SUPER_ADMIN")) {
            throw new IllegalArgumentException("Cannot create super admin users");
        }
        
        if (user.getRole().contains("ROLE_ADMIN") && !currentUserRole.equals("ROLE_SUPER_ADMIN")) {
            throw new IllegalArgumentException("Only super admin can create admin users");
        }
        
        // Check if email already exists
        if (emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Check if phone already exists
        if (phoneExists(user.getPhone_number())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        
        // Hash password with our PasswordHasher au format $2y$
        String hashedPassword = PasswordHasher.hashPassword(user.getPassword());
        
        // Convert roles to JSON
        String rolesJson = convertRolesToJson(user.getRole());
        
        String query = "INSERT INTO user (name, email, phone_number, password, is_verified, is_blocked, created_at, roles) VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone_number());
            statement.setString(4, hashedPassword);
            statement.setBoolean(5, user.isVerified());
            statement.setBoolean(6, user.isBlocked());
            statement.setString(7, rolesJson);
            
            statement.executeUpdate();
        }
    }
    
    /**
     * Check if email exists in database
     * @param email The email to check
     * @return true if email exists, false otherwise
     * @throws SQLException In case of SQL error
     */
    private boolean emailExists(String email) throws SQLException {
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
     * Check if phone number exists in database
     * @param phone The phone number to check
     * @return true if phone exists, false otherwise
     * @throws SQLException In case of SQL error
     */
    private boolean phoneExists(String phone) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE phone_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, phone);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
     * Convert list of roles to JSON string
     * @param roles List of roles
     * @return JSON string representation
     */
    private String convertRolesToJson(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "[\"ROLE_USER\"]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < roles.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(roles.get(i)).append("\"");
        }
        json.append("]");
        
        return json.toString();
    }

    /**
     * Update user role (used for role toggling by super admin)
     * @param userId User ID
     * @param newRole New role to assign
     * @throws SQLException In case of SQL error
     */
    public void updateUserRole(int userId, String newRole) throws SQLException {
        // Create a list with the single new role
        List<String> roles = new ArrayList<>();
        roles.add(newRole);
        
        // Convert to JSON and update in database
        String rolesJson = convertRolesToJson(roles);
        String query = "UPDATE user SET roles = ? WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, rolesJson);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    /**
     * Delete user (only regular users can be deleted, not admins or super admins)
     * @param userId User ID to delete
     * @throws SQLException In case of SQL error
     * @throws IllegalArgumentException If trying to delete admin or super admin
     */
    public void deleteUser(int userId) throws SQLException, IllegalArgumentException {
        // First check if the user is a client (ROLE_USER)
        User user = getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Only clients (ROLE_USER) can be deleted
        if (!user.getRole().contains("ROLE_USER") || user.getRole().contains("ROLE_ADMIN") || user.getRole().contains("ROLE_SUPER_ADMIN")) {
            throw new IllegalArgumentException("Only client users can be deleted");
        }
        
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }
}
