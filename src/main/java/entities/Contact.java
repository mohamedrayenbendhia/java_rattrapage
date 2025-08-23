package entities;

import java.time.LocalDateTime;

public class Contact {
    private int id;
    private String userEmail;
    private String subject;
    private String content;
    private String status; // approved, pending, rejected
    private LocalDateTime createdAt;

    // Default constructor
    public Contact() {
        this.createdAt = LocalDateTime.now();
        this.status = "pending";
    }

    // Constructor with parameters
    public Contact(String userEmail, String subject, String content) {
        this.userEmail = userEmail;
        this.subject = subject;
        this.content = content;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
