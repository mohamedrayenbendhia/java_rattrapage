package entities;

import java.time.LocalDateTime;

public class Rating {
    private int id;
    private int raterId;
    private int ratedId;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;
    
    // Informations supplémentaires pour l'affichage
    private String raterName;
    private String ratedName;

    public Rating() {
        this.createdAt = LocalDateTime.now();
    }

    public Rating(int raterId, int ratedId, int stars, String comment) {
        this();
        this.raterId = raterId;
        this.ratedId = ratedId;
        this.stars = stars;
        this.comment = comment;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRaterId() {
        return raterId;
    }

    public void setRaterId(int raterId) {
        this.raterId = raterId;
    }

    public int getRatedId() {
        return ratedId;
    }

    public void setRatedId(int ratedId) {
        this.ratedId = ratedId;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRaterName() {
        return raterName;
    }

    public void setRaterName(String raterName) {
        this.raterName = raterName;
    }

    public String getRatedName() {
        return ratedName;
    }

    public void setRatedName(String ratedName) {
        this.ratedName = ratedName;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id=" + id +
                ", raterId=" + raterId +
                ", ratedId=" + ratedId +
                ", stars=" + stars +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
