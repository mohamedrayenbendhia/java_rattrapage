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
                userInfoText.setText("Connecté en tant que: " + currentUser.getName());
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
                        phoneLabel.setText("📞 " + (user.getPhone_number() != null ? user.getPhone_number() : "Non renseigné"));
                        
                        try {
                            double avgRating = ratingService.getAverageRating(user.getId());
                            ratingLabel.setText("⭐ " + String.format("%.1f", avgRating));
                        } catch (Exception e) {
                            ratingLabel.setText("⭐ N/A");
                        }
                        
                        try {
                            if (ratingService.hasUserRatedUser(currentUser.getId(), user.getId())) {
                                rateButton.setText("Noté ✓");
                                rateButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                                rateButton.setDisable(true);
                            } else {
                                rateButton.setText("Noter");
                                rateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                                rateButton.setDisable(false);
                                rateButton.setOnAction(e -> showRatingDialog(user));
                            }
                        } catch (Exception ex) {
                            rateButton.setText("Erreur");
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
                Label title = new Label("Liste des clients (" + clients.size() + " clients)");
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
            
            // Create table
            TableView<Rating> ratingsTable = new TableView<>();
            
            TableColumn<Rating, String> userColumn = new TableColumn<>("Client noté");
            userColumn.setCellValueFactory(new PropertyValueFactory<>("ratedName"));
            userColumn.setPrefWidth(200);
            
            TableColumn<Rating, Integer> starsColumn = new TableColumn<>("Note");
            starsColumn.setCellValueFactory(new PropertyValueFactory<>("stars"));
            starsColumn.setPrefWidth(80);
            
            TableColumn<Rating, String> commentColumn = new TableColumn<>("Commentaire");
            commentColumn.setCellValueFactory(cellData -> {
                String comment = cellData.getValue().getComment();
                return new SimpleStringProperty(comment != null && !comment.trim().isEmpty() ? comment : "Aucun commentaire");
            });
            commentColumn.setPrefWidth(300);
            
            TableColumn<Rating, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cellData -> {
                return new SimpleStringProperty(
                    cellData.getValue().getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            });
            dateColumn.setPrefWidth(120);

            ratingsTable.getColumns().addAll(userColumn, starsColumn, commentColumn, dateColumn);
            
            ObservableList<Rating> ratingsList = FXCollections.observableArrayList(givenRatings);
            ratingsTable.setItems(ratingsList);
            
            // Update content area
            Platform.runLater(() -> {
                ratingContentArea.getChildren().clear();
                Label title = new Label("Mes ratings donnés (" + givenRatings.size() + " ratings)");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                ratingContentArea.getChildren().addAll(title, ratingsTable);
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
            
            // Create table
            TableView<Rating> ratingsTable = new TableView<>();
            
            TableColumn<Rating, String> userColumn = new TableColumn<>("Client qui a noté");
            userColumn.setCellValueFactory(new PropertyValueFactory<>("raterName"));
            userColumn.setPrefWidth(200);
            
            TableColumn<Rating, Integer> starsColumn = new TableColumn<>("Note reçue");
            starsColumn.setCellValueFactory(new PropertyValueFactory<>("stars"));
            starsColumn.setPrefWidth(100);
            
            TableColumn<Rating, String> commentColumn = new TableColumn<>("Commentaire");
            commentColumn.setCellValueFactory(cellData -> {
                String comment = cellData.getValue().getComment();
                return new SimpleStringProperty(comment != null && !comment.trim().isEmpty() ? comment : "Aucun commentaire");
            });
            commentColumn.setPrefWidth(300);
            
            TableColumn<Rating, String> dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(cellData -> {
                return new SimpleStringProperty(
                    cellData.getValue().getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            });
            dateColumn.setPrefWidth(120);

            ratingsTable.getColumns().addAll(userColumn, starsColumn, commentColumn, dateColumn);
            
            ObservableList<Rating> ratingsList = FXCollections.observableArrayList(receivedRatings);
            ratingsTable.setItems(ratingsList);
            
            // Update content area
            Platform.runLater(() -> {
                ratingContentArea.getChildren().clear();
                Label title = new Label("Mes ratings reçus (" + receivedRatings.size() + " ratings)");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                ratingContentArea.getChildren().addAll(title, ratingsTable);
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
            Label message = new Label("Données actualisées ! Sélectionnez une action ci-dessus.");
            message.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60;");
            ratingContentArea.getChildren().add(message);
        });
        
        showAlert(Alert.AlertType.INFORMATION, "Actualisation", "Les données ont été actualisées avec succès !");
    }

    private void showRatingDialog(User userToRate) {
        System.out.println("Opening rating dialog for: " + userToRate.getName());
        
        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Noter l'utilisateur");
        dialog.setHeaderText("Noter " + userToRate.getName());

        ButtonType rateButtonType = new ButtonType("Soumettre", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rateButtonType, ButtonType.CANCEL);

        VBox formContainer = new VBox(15);
        formContainer.setStyle("-fx-padding: 20;");

        Label starsLabel = new Label("Note (1-5 étoiles) :");
        starsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<Integer> starsComboBox = new ComboBox<>();
        starsComboBox.getItems().addAll(1, 2, 3, 4, 5);
        starsComboBox.setValue(5);

        Label commentLabel = new Label("Commentaire (optionnel) :");
        commentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Entrez votre commentaire ici (optionnel)...");
        commentArea.setPrefRowCount(4);

        formContainer.getChildren().addAll(starsLabel, starsComboBox, commentLabel, commentArea);
        dialog.getDialogPane().setContent(formContainer);

        Node rateButton = dialog.getDialogPane().lookupButton(rateButtonType);
        rateButton.addEventFilter(ActionEvent.ACTION, event -> {
            Integer stars = starsComboBox.getValue();
            String comment = commentArea.getText().trim();

            if (stars == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", "Veuillez sélectionner une note.");
                event.consume();
                return;
            }

            try {
                User currentUser = entities.UserSession.getInstance().getCurrentUser();
                Rating rating = new Rating(currentUser.getId(), userToRate.getId(), stars, comment);
                ratingService.addRating(rating);
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Note soumise avec succès !");
                loadRatingStatistics(); // Refresh statistics
                
            } catch (Exception e) {
                System.out.println("Error submitting rating: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la soumission : " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }
}
