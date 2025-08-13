package services;

import entities.User;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service pour gérer les rôles des utilisateurs
 */
public class RoleService {
    private static RoleService instance;
    private final Connection connection;

    // Constants for roles
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER"; // Previously CLIENT

    /**
     * Constructeur privé pour le pattern Singleton
     */
    private RoleService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Obtenir l'instance unique du service
     * @return L'instance du service
     */
    public static RoleService getInstance() {
        if (instance == null) {
            instance = new RoleService();
        }
        return instance;
    }

    /**
     * Récupérer les rôles d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return La liste des rôles de l'utilisateur
     * @throws SQLException En cas d'erreur SQL
     */
    public List<String> getUserRoles(int userId) throws SQLException {
        List<String> roles = new ArrayList<>();
        String query = "SELECT roles FROM user WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String rolesJson = resultSet.getString("roles");
                    if (rolesJson != null && !rolesJson.isEmpty()) {
                        roles = parseRolesFromJson(rolesJson);
                    }
                }
            }
        }
        
        return roles;
    }

    /**
     * Ajouter un rôle à un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param roleName Le nom du rôle à ajouter
     * @throws SQLException En cas d'erreur SQL
     */
    public void addRoleToUser(int userId, String roleName) throws SQLException {
        // Récupérer les rôles actuels
        List<String> currentRoles = getUserRoles(userId);
        
        // Vérifier si l'utilisateur a déjà ce rôle
        if (currentRoles.contains(roleName)) {
            return; // L'utilisateur a déjà ce rôle
        }
        
        // Ajouter le nouveau rôle
        currentRoles.add(roleName);
        
        // Mettre à jour les rôles dans la base de données
        updateUserRoles(userId, currentRoles);
    }

    /**
     * Supprimer un rôle d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param roleName Le nom du rôle à supprimer
     * @throws SQLException En cas d'erreur SQL
     */
    public void removeRoleFromUser(int userId, String roleName) throws SQLException {
        // Récupérer les rôles actuels
        List<String> currentRoles = getUserRoles(userId);
        
        // Vérifier si l'utilisateur a ce rôle
        if (!currentRoles.contains(roleName)) {
            return; // L'utilisateur n'a pas ce rôle
        }
        
        // Supprimer le rôle
        currentRoles.remove(roleName);
        
        // Mettre à jour les rôles dans la base de données
        updateUserRoles(userId, currentRoles);
    }

    /**
     * Vérifier si un utilisateur a un rôle spécifique
     * @param user L'utilisateur
     * @param role Le rôle à vérifier
     * @return true si l'utilisateur a le rôle, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean hasRole(User user, String role) throws SQLException {
        if (user == null) {
            return false;
        }
        
        List<String> roles = getUserRoles(user.getId());
        return roles.contains(role);
    }

    /**
     * Vérifier si un utilisateur est un administrateur
     * @param user L'utilisateur
     * @return true si l'utilisateur est un administrateur, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean isAdmin(User user) throws SQLException {
        return hasRole(user, ROLE_ADMIN) || hasRole(user, ROLE_SUPER_ADMIN);
    }

    /**
     * Vérifier si un utilisateur est un super administrateur
     * @param user L'utilisateur
     * @return true si l'utilisateur est un super administrateur, false sinon
     * @throws SQLException En cas d'erreur SQL
     */
    public boolean isSuperAdmin(User user) throws SQLException {
        return hasRole(user, ROLE_SUPER_ADMIN);
    }

    /**
     * Check if a user is a regular user
     * @param user The user
     * @return true if the user is a regular user, false otherwise
     * @throws SQLException In case of SQL error
     */
    public boolean isUser(User user) throws SQLException {
        return hasRole(user, ROLE_USER);
    }

    /**
     * Determine user type (admin, super admin or user)
     * @param user The user
     * @return The user type (ROLE_ADMIN, ROLE_SUPER_ADMIN, ROLE_USER or null if no role)
     * @throws SQLException En cas d'erreur SQL
     */
    public String getUserType(User user) throws SQLException {
        if (user == null) {
            return null;
        }
        
        if (isSuperAdmin(user)) {
            return ROLE_SUPER_ADMIN;
        } else if (isAdmin(user)) {
            return ROLE_ADMIN;
        } else if (isUser(user)) {
            return ROLE_USER;
        }
        
        return null;
    }
    
    /**
     * Mettre à jour les rôles d'un utilisateur dans la base de données
     * @param userId L'ID de l'utilisateur
     * @param roles La liste des rôles
     * @throws SQLException En cas d'erreur SQL
     */
    private void updateUserRoles(int userId, List<String> roles) throws SQLException {
        String rolesJson = convertRolesToJson(roles);
        String query = "UPDATE user SET roles = ? WHERE id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, rolesJson);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }
    
    /**
     * Convertir une liste de rôles en JSON
     * @param roles La liste des rôles
     * @return La chaîne JSON
     */
    private String convertRolesToJson(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "[\"ROLE_USER\"]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < roles.size(); i++) {
            json.append("\"").append(roles.get(i)).append("\"");
            if (i < roles.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        
        return json.toString();
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
}
