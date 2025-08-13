package services;

import entities.Rating;
import entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RatingService {
    private final Connection connection;

    public RatingService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Add a new rating
     */
    public void addRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO rating (rater_id, rated_id, stars, comment, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating.getRaterId());
            stmt.setInt(2, rating.getRatedId());
            stmt.setInt(3, rating.getStars());
            stmt.setString(4, rating.getComment());
            stmt.setTimestamp(5, Timestamp.valueOf(rating.getCreatedAt()));
            
            stmt.executeUpdate();
        }
    }

    /**
     * Get all ratings given by a user
     */
    public List<Rating> getRatingsGivenByUser(int userId) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = """
            SELECT r.*, u.name as rated_name 
            FROM rating r 
            JOIN user u ON r.rated_id = u.id 
            WHERE r.rater_id = ? 
            ORDER BY r.created_at DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setRaterId(rs.getInt("rater_id"));
                rating.setRatedId(rs.getInt("rated_id"));
                rating.setStars(rs.getInt("stars"));
                rating.setComment(rs.getString("comment"));
                rating.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                rating.setRatedName(rs.getString("rated_name"));
                ratings.add(rating);
            }
        }
        
        return ratings;
    }

    /**
     * Get all ratings received by a user
     */
    public List<Rating> getRatingsReceivedByUser(int userId) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = """
            SELECT r.*, u.name as rater_name 
            FROM rating r 
            JOIN user u ON r.rater_id = u.id 
            WHERE r.rated_id = ? 
            ORDER BY r.created_at DESC
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Rating rating = new Rating();
                rating.setId(rs.getInt("id"));
                rating.setRaterId(rs.getInt("rater_id"));
                rating.setRatedId(rs.getInt("rated_id"));
                rating.setStars(rs.getInt("stars"));
                rating.setComment(rs.getString("comment"));
                rating.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                rating.setRaterName(rs.getString("rater_name"));
                ratings.add(rating);
            }
        }
        
        return ratings;
    }

    /**
     * Get count of ratings given by user
     */
    public int getRatingsGivenCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rating WHERE rater_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * Get count of ratings received by user
     */
    public int getRatingsReceivedCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rating WHERE rated_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }

    /**
     * Check if user has already rated another user
     */
    public boolean hasUserRatedUser(int raterId, int ratedId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rating WHERE rater_id = ? AND rated_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, raterId);
            stmt.setInt(2, ratedId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        
        return false;
    }

    /**
     * Get all users except the current user (for client list)
     */
    public List<User> getAllUsersExceptCurrent(int currentUserId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT id, name, email, phone_number, is_verified, is_blocked, roles 
            FROM user 
            WHERE id != ? AND JSON_CONTAINS(roles, '"ROLE_USER"')
            ORDER BY name
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPhone_number(rs.getString("phone_number"));
                user.setVerified(rs.getBoolean("is_verified"));
                user.setBlocked(rs.getBoolean("is_blocked"));
                
                // Parse roles JSON
                String rolesJson = rs.getString("roles");
                List<String> roles = new ArrayList<>();
                if (rolesJson.contains("ROLE_USER")) roles.add("ROLE_USER");
                if (rolesJson.contains("ROLE_ADMIN")) roles.add("ROLE_ADMIN");
                if (rolesJson.contains("ROLE_SUPER_ADMIN")) roles.add("ROLE_SUPER_ADMIN");
                user.setRole(roles);
                
                users.add(user);
            }
        }
        
        return users;
    }

    /**
     * Get average rating for a user
     */
    public double getAverageRating(int userId) throws SQLException {
        String sql = "SELECT AVG(stars) FROM rating WHERE rated_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        
        return 0.0;
    }
}
