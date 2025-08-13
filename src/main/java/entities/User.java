package entities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String email;
    private String phone_number;
    private boolean isVerified;
    private boolean isBlocked;
    private Timestamp created_at;
    private List<String> role;
    private String password;
    private String image;
    private String secretKey; // Secret key for two-factor authentication

    // Default constructor
    public User() {
        this.role = new ArrayList<>();
        this.isBlocked = false; // Valeur par défaut
    }

    // Constructor with all parameters
    public User(int id, String name, String email, String phone_number, boolean isVerified, Timestamp created_at, String password, String image, String secretKey) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone_number = phone_number;
        this.isVerified = isVerified;
        this.isBlocked = false; // Valeur par défaut
        this.created_at = created_at;
        this.password = password;
        this.image = image;
        this.secretKey = secretKey;
        this.role = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", isVerified=" + isVerified +
                ", isBlocked=" + isBlocked +
                ", created_at=" + created_at +
                ", role=" + role +
                ", image='" + image + '\'' +
                ", secretKey='" + (secretKey != null ? "***" : "null") + '\'' +
                '}';
    }
}
