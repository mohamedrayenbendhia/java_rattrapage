package services;

import entities.Contact;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContactService implements IService<Contact> {

    private Connection connection;

    public ContactService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Contact contact) throws SQLException {
        String query = "INSERT INTO contact (user_email, subject, content, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, contact.getUserEmail());
            ps.setString(2, contact.getSubject());
            ps.setString(3, contact.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(contact.getCreatedAt()));
            ps.executeUpdate();
            System.out.println("Contact message sent successfully!");
        } catch (SQLException e) {
            System.err.println("Error sending contact message: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(Contact contact) throws SQLException {
        String query = "UPDATE contact SET user_email = ?, subject = ?, content = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, contact.getUserEmail());
            ps.setString(2, contact.getSubject());
            ps.setString(3, contact.getContent());
            ps.setInt(4, contact.getId());
            ps.executeUpdate();
            System.out.println("Contact updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating contact: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void supprimer(Contact contact) throws SQLException {
        String query = "DELETE FROM contact WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, contact.getId());
            ps.executeUpdate();
            System.out.println("Contact deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Contact> afficher() throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT * FROM contact ORDER BY created_at DESC";
        try (Statement st = connection.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                contacts.add(mapResultSetToContact(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all contacts: " + e.getMessage());
            throw e;
        }
        return contacts;
    }

    // Méthode utilitaire pour récupérer un contact par ID
    public Contact getContactById(int id) throws SQLException {
        String query = "SELECT * FROM contact WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToContact(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving contact: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public List<Contact> getContactsByUserEmail(String userEmail) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT * FROM contact WHERE user_email = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, userEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                contacts.add(mapResultSetToContact(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving contacts for user: " + e.getMessage());
            throw e;
        }
        return contacts;
    }

    private Contact mapResultSetToContact(ResultSet rs) throws SQLException {
        Contact contact = new Contact();
        contact.setId(rs.getInt("id"));
        contact.setUserEmail(rs.getString("user_email"));
        contact.setSubject(rs.getString("subject"));
        contact.setContent(rs.getString("content"));
        contact.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return contact;
    }
}
