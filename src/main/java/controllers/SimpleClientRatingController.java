package controllers;

import entities.Rating;
import entities.User;
import entities.UserSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.RatingService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ClientRatingController implements Initializable {

    @FXML private Label ratingsGivenLabel;
    @FXML private Label ratingsReceivedLabel;
    @FXML private Button logoutButton;
    @FXML private Button refreshButton;
    @FXML private TabPane mainTabPane;

    // Client List Tab
    @FXML private TableView<User> clientsTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> averageRatingColumn;
    @FXML private TableColumn<User, Void> actionColumn;

    // My Ratings Tab
    @FXML private TableView<Rating> givenRatingsTable;
    @FXML private TableColumn<Rating, String> ratedUserColumn;
    @FXML private TableColumn<Rating, Integer> starsGivenColumn;
    @FXML private TableColumn<Rating, String> commentGivenColumn;
    @FXML private TableColumn<Rating, String> dateGivenColumn;

    // Received Ratings Tab
    @FXML private TableView<Rating> receivedRatingsTable;
    @FXML private TableColumn<Rating, String> raterUserColumn;
    @FXML private TableColumn<Rating, Integer> starsReceivedColumn;
    @FXML private TableColumn<Rating, String> commentReceivedColumn;
    @FXML private TableColumn<Rating, String> dateReceivedColumn;

    private RatingService ratingService;
    private User currentUser;
    private ObservableList<User> clientsList;
    private ObservableList<Rating> givenRatingsList;
    private ObservableList<Rating> receivedRatingsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ClientRatingController: Initializing...");
        
        try {
            ratingService = new RatingService();
            currentUser = UserSession.getInstance().getCurrentUser();
            
            if (currentUser == null) {
                System.out.println("ERROR: No user session found!");
                showAlert(Alert.AlertType.ERROR, "Error", "No user session found. Please login again.");
                handleLogout();
                return;
            }
            
            System.out.println("Current user: " + currentUser.getName());
            
            initializeTables();
            loadData();
            
            System.out.println("ClientRatingController: Initialization complete!");
            
        } catch (Exception e) {
            System.out.println("ERROR during initialization: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error during initialization: " + e.getMessage());
        }
    }

    private void initializeTables() {
        System.out.println("Initializing tables...");
        
        // Initialize clients table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone_number"));
        
        averageRatingColumn.setCellValueFactory(cellData -> {
            try {
                double avgRating = ratingService.getAverageRating(cellData.getValue().getId());
                return new SimpleStringProperty(String.format("%.1f ⭐", avgRating));
            } catch (SQLException e) {
                return new SimpleStringProperty("N/A");
            }
        });

        // Add action buttons to clients table
        actionColumn.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button rateButton = new Button("Rate");

            {
                rateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                rateButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showRatingDialog(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    try {
                        if (ratingService.hasUserRatedUser(currentUser.getId(), user.getId())) {
                            rateButton.setText("Rated ✓");
                            rateButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                            rateButton.setDisable(true);
                        } else {
                            rateButton.setText("Rate");
                            rateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                            rateButton.setDisable(false);
                        }
                    } catch (SQLException e) {
                        rateButton.setText("Error");
                        rateButton.setDisable(true);
                    }
                    setGraphic(rateButton);
                }
            }
        });

        // Initialize given ratings table
        ratedUserColumn.setCellValueFactory(new PropertyValueFactory<>("ratedName"));
        starsGivenColumn.setCellValueFactory(new PropertyValueFactory<>("stars"));
        commentGivenColumn.setCellValueFactory(cellData -> {
            String comment = cellData.getValue().getComment();
            return new SimpleStringProperty(comment != null && !comment.trim().isEmpty() ? comment : "No comment");
        });
        dateGivenColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(
                cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
        });

        // Initialize received ratings table
        raterUserColumn.setCellValueFactory(new PropertyValueFactory<>("raterName"));
        starsReceivedColumn.setCellValueFactory(new PropertyValueFactory<>("stars"));
        commentReceivedColumn.setCellValueFactory(cellData -> {
            String comment = cellData.getValue().getComment();
            return new SimpleStringProperty(comment != null && !comment.trim().isEmpty() ? comment : "No comment");
        });
        dateReceivedColumn.setCellValueFactory(cellData -> {
            return new SimpleStringProperty(
                cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
        });

        // Initialize lists
        clientsList = FXCollections.observableArrayList();
        givenRatingsList = FXCollections.observableArrayList();
        receivedRatingsList = FXCollections.observableArrayList();

        clientsTable.setItems(clientsList);
        givenRatingsTable.setItems(givenRatingsList);
        receivedRatingsTable.setItems(receivedRatingsList);
        
        System.out.println("Tables initialized successfully!");
    }

    private void loadData() {
        System.out.println("Loading data...");
        
        try {
            // Load statistics
            int ratingsGiven = ratingService.getRatingsGivenCount(currentUser.getId());
            int ratingsReceived = ratingService.getRatingsReceivedCount(currentUser.getId());
            
            System.out.println("Ratings given: " + ratingsGiven + ", Ratings received: " + ratingsReceived);
            
            Platform.runLater(() -> {
                ratingsGivenLabel.setText(String.valueOf(ratingsGiven));
                ratingsReceivedLabel.setText(String.valueOf(ratingsReceived));
            });

            // Load clients list (excluding current user)
            List<User> clients = ratingService.getAllUsersExceptCurrent(currentUser.getId());
            System.out.println("Found " + clients.size() + " other clients");
            
            Platform.runLater(() -> {
                clientsList.clear();
                clientsList.addAll(clients);
            });

            // Load given ratings
            List<Rating> givenRatings = ratingService.getRatingsGivenByUser(currentUser.getId());
            System.out.println("Found " + givenRatings.size() + " given ratings");
            
            Platform.runLater(() -> {
                givenRatingsList.clear();
                givenRatingsList.addAll(givenRatings);
            });

            // Load received ratings
            List<Rating> receivedRatings = ratingService.getRatingsReceivedByUser(currentUser.getId());
            System.out.println("Found " + receivedRatings.size() + " received ratings");
            
            Platform.runLater(() -> {
                receivedRatingsList.clear();
                receivedRatingsList.addAll(receivedRatings);
            });

            System.out.println("Data loaded successfully!");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading data: " + e.getMessage());
            });
        }
    }

    private void showRatingDialog(User userToRate) {
        System.out.println("Opening rating dialog for: " + userToRate.getName());
        
        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Rate User");
        dialog.setHeaderText("Rate " + userToRate.getName());

        // Configure buttons
        ButtonType rateButtonType = new ButtonType("Submit Rating", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(rateButtonType, ButtonType.CANCEL);

        // Create form
        VBox formContainer = new VBox(15);
        formContainer.setStyle("-fx-padding: 20;");

        // Stars selection
        Label starsLabel = new Label("Rating (1-5 stars):");
        starsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<Integer> starsComboBox = new ComboBox<>();
        starsComboBox.getItems().addAll(1, 2, 3, 4, 5);
        starsComboBox.setValue(5);

        // Comment
        Label commentLabel = new Label("Comment (optional):");
        commentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Enter your comment here (optional)...");
        commentArea.setPrefRowCount(4);

        formContainer.getChildren().addAll(starsLabel, starsComboBox, commentLabel, commentArea);
        dialog.getDialogPane().setContent(formContainer);

        // Handle submission
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
                Rating rating = new Rating(currentUser.getId(), userToRate.getId(), stars, comment);
                ratingService.addRating(rating);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Rating submitted successfully!");
                loadData(); // Refresh data
                
            } catch (SQLException e) {
                System.out.println("Error submitting rating: " + e.getMessage());
                showAlert(Alert.AlertType.ERROR, "Error", "Error submitting rating: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing data...");
        loadData();
        showAlert(Alert.AlertType.INFORMATION, "Refreshed", "Data has been refreshed.");
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out...");
        try {
            UserSession.getInstance().logout();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Pi Dev - Home");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
