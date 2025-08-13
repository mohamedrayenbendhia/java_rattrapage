package controllers;

import entities.User;
import entities.Rating;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.AuthService;
import services.RoleService;
import services.RatingService;
import utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClientDashboardController implements Initializable {

    // Instance statique pour permettre le rafraîchissement des statistiques depuis d'autres contrôleurs
    private static ClientDashboardController instance;

    @FXML
    private Text userInfoText;

    @FXML
    private Text ratingsGivenLabel;

    @FXML
    private Text ratingsReceivedLabel;

    @FXML
    private VBox ratingContentArea;

    private AuthService authService;
    private RoleService roleService;
    private RatingService ratingService;

    public ClientDashboardController() {
        authService = AuthService.getInstance();
        roleService = RoleService.getInstance();
        try {
            ratingService = new RatingService();
        } catch (Exception e) {
            System.out.println("Error initializing RatingService: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        loadUserProfile();
        loadRatingStatistics();
    }

    /**
     * Charge le profil de l'utilisateur connecté
     */
    private void loadUserProfile() {
        try {
            User currentUser = entities.UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                userInfoText.setText("Logged in as: " + currentUser.getName());
            }
        } catch (Exception e) {
            System.out.println("Error loading user profile: " + e.getMessage());
        }
    }

    /**
     * Rafraîchit les statistiques
     * Méthode publique pouvant être appelée depuis d'autres contrôleurs
     */
    public void refreshStatistics() {
        loadRatingStatistics();
    }

    /**
     * Méthode statique pour rafraîchir les statistiques depuis n'importe quel contrôleur
     */
    public static void refreshDashboardStatistics() {
        if (instance != null) {
            instance.refreshStatistics();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Déconnecter l'utilisateur
        authService.logout();

        try {
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) userInfoText.getScene().getWindow();
            
            // Utiliser SceneManager pour naviguer avec une taille fixe
            SceneManager.navigateToPage(stage, "/fxml/Login.fxml", "Connexion");
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de navigation", "Impossible de charger la page de connexion.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExit(ActionEvent event) {
        // Quitter l'application
        Platform.exit();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ============ RATING METHODS ============

    private void loadRatingStatistics() {
        System.out.println("Loading rating statistics...");
        if (ratingService == null) {
            System.out.println("RatingService is null, skipping rating statistics");
            return;
        }

        try {
            User currentUser = entities.UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                int ratingsGiven = ratingService.getRatingsGivenCount(currentUser.getId());
                int ratingsReceived = ratingService.getRatingsReceivedCount(currentUser.getId());
                
                Platform.runLater(() -> {
                    ratingsGivenLabel.setText(String.valueOf(ratingsGiven));
                    ratingsReceivedLabel.setText(String.valueOf(ratingsReceived));
                });
                
                System.out.println("Rating statistics loaded: Given=" + ratingsGiven + ", Received=" + ratingsReceived);
            }
        } catch (Exception e) {
            System.out.println("Error loading rating statistics: " + e.getMessage());
            Platform.runLater(() -> {
                ratingsGivenLabel.setText("0");
                ratingsReceivedLabel.setText("0");
            });
        }
    }

    @FXML
    public void handleViewClients(ActionEvent event) {
        System.out.println("Loading clients list...");
        try {
            User currentUser = entities.UserSession.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No user session found");
                return;
            }

            List<User> clients = ratingService.getAllUsersExceptCurrent(currentUser.getId());
            
            // Create ListView
            ListView<User> clientsListView = new ListView<>();
            clientsListView.setPrefHeight(400);
            
            // Custom cell factory for displaying client info and rating button
            clientsListView.setCellFactory(listView -> new ListCell<User>() {
                private final Button rateButton = new Button("Noter");
                private final HBox hbox = new HBox(10);
                private final VBox clientInfo = new VBox(5);
                private final Label nameLabel = new Label();
                private final Label emailLabel = new Label();
                private final Label phoneLabel = new Label();
                private final Label ratingLabel = new Label();

                {
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    emailLabel.setStyle("-fx-text-fill: #666;");
                    phoneLabel.setStyle("-fx-text-fill: #666;");
                    ratingLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    
                    rateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                    
                    clientInfo.getChildren().addAll(nameLabel, emailLabel, phoneLabel, ratingLabel);
                    hbox.getChildren().addAll(clientInfo, rateButton);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    // Add some padding
                    hbox.setPadding(new javafx.geometry.Insets(10));
                }

                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    
                    if (empty || user == null) {
                        setGraphic(null);
                    } else {
                        nameLabel.setText(user.getName());
                        emailLabel.setText("📧 " + user.getEmail());
                        phoneLabel.setText("📞 " + (user.getPhone_number() != null ? user.getPhone_number() : "Not provided"));
                        
                        try {
                            double avgRating = ratingService.getAverageRating(user.getId());
                            ratingLabel.setText("⭐ " + String.format("%.1f", avgRating));
                        } catch (Exception e) {
                            ratingLabel.setText("⭐ N/A");
                        }
                        
                        try {
                            if (ratingService.hasUserRatedUser(currentUser.getId(), user.getId())) {
                                rateButton.setText("Rated ✓");
                                rateButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                                rateButton.setDisable(true);
                            } else {
                                rateButton.setText("Rate");
                                rateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                                rateButton.setDisable(false);
                                rateButton.setOnAction(e -> showRatingDialog(user));
                            }
                        } catch (Exception ex) {
                            rateButton.setText("Error");
                            rateButton.setDisable(true);
                        }
                        
                        setGraphic(hbox);
                    }
                }
            });
            
            ObservableList<User> clientsList = FXCollections.observableArrayList(clients);
            clientsListView.setItems(clientsList);
            
            // Update content area
            Platform.runLater(() -> {
                ratingContentArea.getChildren().clear();
                Label title = new Label("Client List (" + clients.size() + " clients)");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                ratingContentArea.getChildren().addAll(title, clientsListView);
            });
            
            System.out.println("Clients ListView loaded with " + clients.size() + " clients");
            
        } catch (Exception e) {
            System.out.println("Error loading clients: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading clients: " + e.getMessage());
        }
    }

    @FXML
    public void handleViewGivenRatings(ActionEvent event) {
        System.out.println("Loading given ratings...");
        try {
            User currentUser = entities.UserSession.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No user session found");
                return;
            }

            List<Rating> givenRatings = ratingService.getRatingsGivenByUser(currentUser.getId());
            
            // Create ListView
            ListView<Rating> ratingsListView = new ListView<>();
            ratingsListView.setPrefHeight(400);
            
            // Custom cell factory for displaying rating info
            ratingsListView.setCellFactory(listView -> new ListCell<Rating>() {
                private final HBox hbox = new HBox(15);
                private final VBox ratingInfo = new VBox(5);
                private final VBox detailsInfo = new VBox(3);
                private final Label clientLabel = new Label();
                private final Label starsLabel = new Label();
                private final Label commentLabel = new Label();
                private final Label dateLabel = new Label();

                {
                    clientLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
                    starsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    commentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-wrap-text: true;");
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
                    
                    ratingInfo.getChildren().addAll(clientLabel, starsLabel);
                    detailsInfo.getChildren().addAll(commentLabel, dateLabel);
                    hbox.getChildren().addAll(ratingInfo, detailsInfo);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    // Add padding
                    hbox.setPadding(new Insets(12));
                    hbox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #e9ecef; -fx-border-radius: 8; -fx-border-width: 1;");
                }

                @Override
                protected void updateItem(Rating rating, boolean empty) {
                    super.updateItem(rating, empty);
                    
                    if (empty || rating == null) {
                        setGraphic(null);
                    } else {
                        clientLabel.setText("👤 " + rating.getRatedName());
                        
                        // Generate stars
                        StringBuilder stars = new StringBuilder();
                        for (int i = 0; i < rating.getStars(); i++) {
                            stars.append("⭐");
                        }
                        starsLabel.setText(stars.toString() + " (" + rating.getStars() + "/5)");
                        
                        String comment = rating.getComment();
                        if (comment != null && !comment.trim().isEmpty()) {
                            commentLabel.setText("💬 " + comment);
                        } else {
                            commentLabel.setText("💬 No comment");
                            commentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #bdc3c7; -fx-font-style: italic;");
                        }
                        
                        dateLabel.setText("📅 " + rating.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        
                        setGraphic(hbox);
                    }
                }
            });
            
            ObservableList<Rating> ratingsList = FXCollections.observableArrayList(givenRatings);
            ratingsListView.setItems(ratingsList);
            
            // Update content area
            Platform.runLater(() -> {
                ratingContentArea.getChildren().clear();
                Label title = new Label("My Given Ratings (" + givenRatings.size() + " ratings)");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                ratingContentArea.getChildren().addAll(title, ratingsListView);
            });
            
            System.out.println("Given ratings loaded: " + givenRatings.size() + " ratings");
            
        } catch (Exception e) {
            System.out.println("Error loading given ratings: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading given ratings: " + e.getMessage());
        }
    }

    @FXML
    public void handleViewReceivedRatings(ActionEvent event) {
        System.out.println("Loading received ratings...");
        try {
            User currentUser = entities.UserSession.getInstance().getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No user session found");
                return;
            }

            List<Rating> receivedRatings = ratingService.getRatingsReceivedByUser(currentUser.getId());
            
            // Create ListView
            ListView<Rating> ratingsListView = new ListView<>();
            ratingsListView.setPrefHeight(400);
            
            // Custom cell factory for displaying received rating info
            ratingsListView.setCellFactory(listView -> new ListCell<Rating>() {
                private final HBox hbox = new HBox(15);
                private final VBox ratingInfo = new VBox(5);
                private final VBox detailsInfo = new VBox(3);
                private final Label clientLabel = new Label();
                private final Label starsLabel = new Label();
                private final Label commentLabel = new Label();
                private final Label dateLabel = new Label();

                {
                    clientLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
                    starsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    commentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-wrap-text: true;");
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6;");
                    
                    ratingInfo.getChildren().addAll(clientLabel, starsLabel);
                    detailsInfo.getChildren().addAll(commentLabel, dateLabel);
                    hbox.getChildren().addAll(ratingInfo, detailsInfo);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    // Add padding and different background color for received ratings
                    hbox.setPadding(new Insets(12));
                    hbox.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 8; -fx-border-color: #ffcc02; -fx-border-radius: 8; -fx-border-width: 1;");
                }

                @Override
                protected void updateItem(Rating rating, boolean empty) {
                    super.updateItem(rating, empty);
                    
                    if (empty || rating == null) {
                        setGraphic(null);
                    } else {
                        clientLabel.setText("👤 From: " + rating.getRaterName());
                        
                        // Generate stars
                        StringBuilder stars = new StringBuilder();
                        for (int i = 0; i < rating.getStars(); i++) {
                            stars.append("⭐");
                        }
                        starsLabel.setText(stars.toString() + " (" + rating.getStars() + "/5)");
                        
                        String comment = rating.getComment();
                        if (comment != null && !comment.trim().isEmpty()) {
                            commentLabel.setText("💬 " + comment);
                        } else {
                            commentLabel.setText("💬 No comment");
                            commentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #bdc3c7; -fx-font-style: italic;");
                        }
                        
                        dateLabel.setText("📅 " + rating.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        
                        setGraphic(hbox);
                    }
                }
            });
            
            ObservableList<Rating> ratingsList = FXCollections.observableArrayList(receivedRatings);
            ratingsListView.setItems(ratingsList);
            
            // Update content area
            Platform.runLater(() -> {
                ratingContentArea.getChildren().clear();
                Label title = new Label("My Received Ratings (" + receivedRatings.size() + " ratings)");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                ratingContentArea.getChildren().addAll(title, ratingsListView);
            });
            
            System.out.println("Received ratings loaded: " + receivedRatings.size() + " ratings");
            
        } catch (Exception e) {
            System.out.println("Error loading received ratings: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading received ratings: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefreshRatings(ActionEvent event) {
        System.out.println("Refreshing ratings data...");
        loadRatingStatistics();
        
        Platform.runLater(() -> {
            ratingContentArea.getChildren().clear();
            Label message = new Label("Data refreshed! Select an action above.");
            message.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60;");
            ratingContentArea.getChildren().add(message);
        });
        
        showAlert(Alert.AlertType.INFORMATION, "Refresh", "Data has been successfully refreshed!");
    }

    @FXML
    public void handleContactUs(ActionEvent event) {
        try {
            // Récupérer le stage depuis un élément de l'interface au lieu de event.getSource()
            Stage stage = (Stage) userInfoText.getScene().getWindow();
            SceneManager.navigateToPage(stage, "/fxml/Contact.fxml", "Contact Us");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open contact page: " + e.getMessage());
        }
    }

    private void showRatingDialog(User userToRate) {
        System.out.println("Opening rating dialog for: " + userToRate.getName());
        
        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Rate User");
        dialog.setHeaderText("Rate " + userToRate.getName());

        ButtonType rateButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rateButtonType, ButtonType.CANCEL);

        VBox formContainer = new VBox(15);
        formContainer.setStyle("-fx-padding: 20;");

        Label starsLabel = new Label("Rating (1-5 stars):");
        starsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<Integer> starsComboBox = new ComboBox<>();
        starsComboBox.getItems().addAll(1, 2, 3, 4, 5);
        starsComboBox.setValue(5);

        Label commentLabel = new Label("Comment (optional):");
        commentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Enter your comment here (optional)...");
        commentArea.setPrefRowCount(4);

        formContainer.getChildren().addAll(starsLabel, starsComboBox, commentLabel, commentArea);
        dialog.getDialogPane().setContent(formContainer);

        Node rateButton = dialog.getDialogPane().lookupButton(rateButtonType);
        rateButton.addEventFilter(ActionEvent.ACTION, event -> {
            Integer stars = starsComboBox.getValue();
            String comment = commentArea.getText().trim();

            if (stars == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Please select a rating.");
                event.consume();
                return;
            }

            try {
                User currentUser = entities.UserSession.getInstance().getCurrentUser();
                Rating rating = new Rating(currentUser.getId(), userToRate.getId(), stars, comment);
                ratingService.addRating(rating);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Rating submitted successfully!");
                loadRatingStatistics(); // Refresh statistics
                
            } catch (Exception e) {
                System.out.println("Error submitting rating: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Error submitting rating: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }
}
