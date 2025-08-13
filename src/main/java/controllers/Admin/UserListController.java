package controllers.Admin;

import entities.User;
import entities.UserSession;
import services.UserService;
import utils.ValidationUtils;
import utils.SceneManager;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UserListController implements Initializable {

    @FXML
    private ListView<User> userListView;

    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button addUserButton;

    @FXML
    private Label messageLabel;

    private UserService userService;
    private ObservableList<User> userList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service utilisateur
        userService = UserService.getInstance();
        userList = FXCollections.observableArrayList();
        
        // Configurer la ListView pour afficher les utilisateurs
        userListView.setItems(userList);
        userListView.setCellFactory(listView -> new UserListCell());
        
        // Charger les utilisateurs
        loadUsers();
    }

    /**
     * Cellule personnalisée pour afficher les utilisateurs
     */
    private class UserListCell extends ListCell<User> {
        @Override
        protected void updateItem(User user, boolean empty) {
            super.updateItem(user, empty);
            
            if (empty || user == null) {
                setGraphic(null);
                setText(null);
            } else {
                // Créer un conteneur pour l'utilisateur
                VBox userContainer = new VBox(5);
                userContainer.setStyle("-fx-padding: 10; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
                
                // Informations principales
                Label nameLabel = new Label("Name: " + user.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label emailLabel = new Label("Email: " + user.getEmail());
                emailLabel.setStyle("-fx-font-size: 12px;");
                
                Label phoneLabel = new Label("Phone: " + user.getPhone_number());
                phoneLabel.setStyle("-fx-font-size: 12px;");
                
                // Statuts
                HBox statusBox = new HBox(10);
                
                Label verifiedLabel = new Label(user.isVerified() ? "Verified" : "Not Verified");
                verifiedLabel.setStyle(user.isVerified() ? 
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;" :
                    "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;");
                
                Label blockedLabel = new Label(user.isBlocked() ? "Blocked" : "Active");
                blockedLabel.setStyle(user.isBlocked() ? 
                    "-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;" :
                    "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 10;");
                
                statusBox.getChildren().addAll(verifiedLabel, blockedLabel);
                
                // Boutons d'action
                HBox actionBox = new HBox(5);
                
                Button editButton = new Button("Edit");
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
                editButton.setOnAction(e -> editUser(user));
                
                actionBox.getChildren().add(editButton);
                
                // Add role toggle button only for super admin
                User currentUser = UserSession.getInstance().getCurrentUser();
                if (currentUser != null && currentUser.getRole().contains("ROLE_SUPER_ADMIN")) {
                    // Only show role toggle for users who are either ADMIN or CLIENT (not SUPER_ADMIN)
                    boolean canToggleRole = !user.getRole().contains("ROLE_SUPER_ADMIN");
                    
                    if (canToggleRole) {
                        boolean isAdmin = user.getRole().contains("ROLE_ADMIN");
                        Button toggleRoleButton = new Button(isAdmin ? "Make Client" : "Make Admin");
                        toggleRoleButton.setStyle(isAdmin ? 
                            "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;" :
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");
                        toggleRoleButton.setOnAction(e -> toggleUserRole(user));
                        actionBox.getChildren().add(toggleRoleButton);
                    }
                }

                // Add block/unblock button for admin and super admin (only for clients/users)
                if (currentUser != null && (currentUser.getRole().contains("ROLE_ADMIN") || currentUser.getRole().contains("ROLE_SUPER_ADMIN"))) {
                    // Only show block button for regular users (ROLE_USER), not for admins or super admins
                    boolean isRegularUser = user.getRole().contains("ROLE_USER") && !user.getRole().contains("ROLE_ADMIN") && !user.getRole().contains("ROLE_SUPER_ADMIN");
                    
                    if (isRegularUser) {
                        Button blockButton = new Button(user.isBlocked() ? "Unblock User" : "Block User");
                        blockButton.setStyle(user.isBlocked() ? 
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;" : // Vert pour Unblock
                            "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;"); // Jaune pour Block
                        blockButton.setOnAction(e -> toggleBlockUser(user));
                        actionBox.getChildren().add(blockButton);

                        // Add delete button for admin and super admin
                        Button deleteButton = new Button("Delete User");
                        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;"); // Rouge pour Delete
                        deleteButton.setOnAction(e -> deleteUser(user));
                        actionBox.getChildren().add(deleteButton);
                    }
                }
                
                userContainer.getChildren().addAll(nameLabel, emailLabel, phoneLabel, statusBox, actionBox);
                setGraphic(userContainer);
            }
        }
    }

    /**
     * Charger la liste des utilisateurs
     */
    private void loadUsers() {
        try {
            // Get current user from session
            User currentUser = UserSession.getInstance().getCurrentUser();
            List<User> users;
            
            if (currentUser != null) {
                // Exclude current user and super admin from list
                users = userService.getAllUsersExcludingCurrentAndSuperAdmin(currentUser.getId());
            } else {
                // Fallback to all users if no session
                users = userService.getAllUsers();
            }
            
            userList.clear();
            userList.addAll(users);
            messageLabel.setText("Number of users: " + users.size());
        } catch (SQLException e) {
            messageLabel.setText("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rafraîchir la liste des utilisateurs
     */
    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    /**
     * Handle add user button click
     */
    @FXML
    private void handleAddUser() {
        // Get current user to check role permissions
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No user logged in");
            return;
        }
        
        // Determine current user's highest role
        String currentUserRole = "ROLE_USER";
        if (currentUser.getRole().contains("ROLE_SUPER_ADMIN")) {
            currentUserRole = "ROLE_SUPER_ADMIN";
        } else if (currentUser.getRole().contains("ROLE_ADMIN")) {
            currentUserRole = "ROLE_ADMIN";
        }
        
        showAddUserDialog(currentUserRole);
    }

    /**
     * Bouton retour
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) userListView.getScene().getWindow();
            SceneManager.navigateToPage(stage, "/fxml/admin/AdminDashboard.fxml", "Admin Dashboard");
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Edit a user
     */
    private void editUser(User user) {
        // Create dialog to edit user
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit information for " + user.getName());

        // Configurer les boutons
        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create main scroll pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(400); // Limiter la hauteur
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Créer les champs de formulaire avec un meilleur style
        VBox formContainer = new VBox(15);
        formContainer.setStyle("-fx-padding: 20; -fx-background-color: #f8f9fa;");
        formContainer.setPrefWidth(380); // Réduire la largeur

        // Section Personal Information
        Label personalInfoTitle = new Label("Personal Information");
        personalInfoTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 8 0;");

        VBox personalInfoBox = new VBox(8); // Réduire l'espacement
        personalInfoBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        TextField nameField = new TextField(user.getName());
        nameField.setPromptText("Full name");
        nameField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        TextField emailField = new TextField(user.getEmail());
        emailField.setPromptText("Email");
        emailField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        TextField phoneField = new TextField(user.getPhone_number());
        phoneField.setPromptText("Phone");
        phoneField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        Label nameLabel = new Label("Full Name:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 12px;");
        
        Label emailLabel = new Label("Email Address:");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 12px;");
        
        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 12px;");

        personalInfoBox.getChildren().addAll(
            nameLabel, nameField,
            emailLabel, emailField,
            phoneLabel, phoneField
        );

        // Section Account Settings
        Label accountSettingsTitle = new Label("Account Settings");
        accountSettingsTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 8 0;");

        VBox accountSettingsBox = new VBox(10); // Réduire l'espacement
        accountSettingsBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        CheckBox verifiedCheckBox = new CheckBox("Account verified");
        verifiedCheckBox.setSelected(user.isVerified());
        verifiedCheckBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        CheckBox blockedCheckBox = new CheckBox("Account blocked");
        blockedCheckBox.setSelected(user.isBlocked());
        blockedCheckBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        accountSettingsBox.getChildren().addAll(verifiedCheckBox, blockedCheckBox);

        formContainer.getChildren().addAll(
            personalInfoTitle, personalInfoBox,
            accountSettingsTitle, accountSettingsBox
        );

        // Add form to scroll pane
        scrollPane.setContent(formContainer);
        dialog.getDialogPane().setContent(scrollPane);
        
        // Set dialog size
        dialog.getDialogPane().setPrefSize(420, 450); // Réduire la taille
        
        // Style the dialog buttons
        dialog.getDialogPane().setStyle("-fx-background-color: #ecf0f1;");
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-size: 12px;");
        
        Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-size: 12px;");

        // Validation et conversion du résultat
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Validation des champs
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            
            if (!ValidationUtils.isValidName(name)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Name must be between 3 and 20 characters");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Email address is not valid");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidPhoneNumber(phone)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Phone number must contain exactly 8 digits");
                event.consume();
                return;
            }

            // Mettre à jour l'utilisateur
            user.setName(name);
            user.setEmail(email);
            user.setPhone_number(phone);
            user.setVerified(verifiedCheckBox.isSelected());
            user.setBlocked(blockedCheckBox.isSelected());

            try {
                userService.updateUser(user);
                loadUsers(); // Rafraîchir la liste
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error updating user: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    /**
     * Toggle user role between ADMIN and CLIENT (only for super admin)
     */
    private void toggleUserRole(User user) {
        // Verify current user is super admin
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null || !currentUser.getRole().contains("ROLE_SUPER_ADMIN")) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only Super Admin can change user roles.");
            return;
        }

        // Confirm role change
        boolean isCurrentlyAdmin = user.getRole().contains("ROLE_ADMIN");
        String newRole = isCurrentlyAdmin ? "Client" : "Admin";
        String currentRole = isCurrentlyAdmin ? "Admin" : "Client";
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Role Change Confirmation");
        confirmAlert.setHeaderText("Change User Role");
        confirmAlert.setContentText("Are you sure you want to change " + user.getName() + 
                                   " from " + currentRole + " to " + newRole + "?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // Update role in database
                    String newRoleCode = isCurrentlyAdmin ? "ROLE_USER" : "ROLE_ADMIN";
                    userService.updateUserRole(user.getId(), newRoleCode);
                    
                    // Update local user object
                    user.getRole().clear();
                    user.getRole().add(newRoleCode);
                    
                    loadUsers(); // Refresh the list
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "User role changed to " + newRole + " successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Error updating user role: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show add user dialog
     */
    private void showAddUserDialog(String currentUserRole) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        // Configure buttons
        ButtonType addButtonType = new ButtonType("Create User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create main scroll pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefHeight(450); // Limiter la hauteur
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Create form fields with better styling
        VBox formContainer = new VBox(15);
        formContainer.setStyle("-fx-padding: 20; -fx-background-color: #f8f9fa;");
        formContainer.setPrefWidth(400); // Réduire la largeur de 500 à 400

        // Section Personal Information
        Label personalInfoTitle = new Label("Personal Information");
        personalInfoTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 8 0;");

        VBox personalInfoBox = new VBox(8); // Réduire l'espacement de 12 à 8
        personalInfoBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");
        nameField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address");
        emailField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number (8 digits)");
        phoneField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        Label nameLabel = new Label("Full Name:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 13px;");
        
        Label emailLabel = new Label("Email Address:");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 13px;");
        
        Label phoneLabel = new Label("Phone Number:");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 13px;");

        personalInfoBox.getChildren().addAll(
            nameLabel, nameField,
            emailLabel, emailField,
            phoneLabel, phoneField
        );

        // Section Security
        Label securityTitle = new Label("Security Settings");
        securityTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 8 0;");

        VBox securityBox = new VBox(8); // Réduire l'espacement
        securityBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password (minimum 8 characters)");
        passwordField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        confirmPasswordField.setStyle("-fx-padding: 10; -fx-font-size: 13px; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 13px;");
        
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 13px;");

        securityBox.getChildren().addAll(
            passwordLabel, passwordField,
            confirmPasswordLabel, confirmPasswordField
        );

        // Section Account Settings
        Label accountSettingsTitle = new Label("Account Settings");
        accountSettingsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 8 0;");

        VBox accountSettingsBox = new VBox(10); // Réduire l'espacement
        accountSettingsBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        Label roleLabel = new Label("User Role:");
        roleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 13px;");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        if (currentUserRole.equals("ROLE_SUPER_ADMIN")) {
            roleComboBox.getItems().addAll("ROLE_USER", "ROLE_ADMIN");
        } else {
            roleComboBox.getItems().add("ROLE_USER");
        }
        roleComboBox.setValue("ROLE_USER");
        roleComboBox.setStyle("-fx-padding: 8; -fx-font-size: 13px; -fx-background-radius: 4;");
        roleComboBox.setPrefWidth(150);
        
        CheckBox verifiedCheckBox = new CheckBox("Account verified");
        verifiedCheckBox.setSelected(false);
        verifiedCheckBox.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        accountSettingsBox.getChildren().addAll(
            roleLabel, roleComboBox,
            verifiedCheckBox
        );

        formContainer.getChildren().addAll(
            personalInfoTitle, personalInfoBox,
            securityTitle, securityBox,
            accountSettingsTitle, accountSettingsBox
        );

        // Add form to scroll pane
        scrollPane.setContent(formContainer);
        dialog.getDialogPane().setContent(scrollPane);
        
        // Set dialog size
        dialog.getDialogPane().setPrefSize(450, 500); // Réduire la taille
        
        // Style the dialog buttons
        dialog.getDialogPane().setStyle("-fx-background-color: #ecf0f1;");
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-size: 13px;");
        
        Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-size: 13px;");

        // Validation and result conversion
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Validate fields
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            String role = roleComboBox.getValue();
            
            if (!ValidationUtils.isValidName(name)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Name must be between 3 and 20 characters");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidEmail(email)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Email address is not valid");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidPhoneNumber(phone)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Phone number must contain exactly 8 digits");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.isValidPassword(password)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Password must be at least 8 characters long");
                event.consume();
                return;
            }
            
            if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", 
                    "Passwords do not match");
                event.consume();
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhone_number(phone);
            newUser.setPassword(password);
            newUser.setVerified(verifiedCheckBox.isSelected());
            newUser.setBlocked(false);
            
            // Set role
            List<String> roles = new ArrayList<>();
            roles.add(role);
            newUser.setRole(roles);

            try {
                userService.addUser(newUser, currentUserRole);
                loadUsers(); // Refresh the list
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                event.consume();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error adding user: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    /**
     * Toggle block/unblock user (for admin and super admin only, only for regular users)
     */
    private void toggleBlockUser(User user) {
        // Verify current user is admin or super admin
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null || (!currentUser.getRole().contains("ROLE_ADMIN") && !currentUser.getRole().contains("ROLE_SUPER_ADMIN"))) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only Admin or Super Admin can block/unblock users.");
            return;
        }

        // Verify target user is a regular user (not admin or super admin)
        if (user.getRole().contains("ROLE_ADMIN") || user.getRole().contains("ROLE_SUPER_ADMIN")) {
            showAlert(Alert.AlertType.ERROR, "Operation Not Allowed", "Cannot block admin or super admin users.");
            return;
        }

        // Confirm block/unblock action
        String action = user.isBlocked() ? "unblock" : "block";
        String actionCapitalized = user.isBlocked() ? "Unblock" : "Block";
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle(actionCapitalized + " User Confirmation");
        confirmAlert.setHeaderText(actionCapitalized + " User Account");
        confirmAlert.setContentText("Are you sure you want to " + action + " " + user.getName() + "'s account?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // Toggle blocked status
                    boolean newBlockedStatus = !user.isBlocked();
                    
                    // Update in database using the specific method for block status
                    userService.updateUserBlockStatus(user.getId(), newBlockedStatus);
                    
                    // Update local user object
                    user.setBlocked(newBlockedStatus);
                    
                    loadUsers(); // Refresh the list
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "User " + user.getName() + " has been " + action + "ed successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Error " + action + "ing user: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }

    /**
     * Delete user (for admin and super admin only, only for regular users)
     */
    private void deleteUser(User user) {
        // Verify current user is admin or super admin
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null || (!currentUser.getRole().contains("ROLE_ADMIN") && !currentUser.getRole().contains("ROLE_SUPER_ADMIN"))) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "Only Admin or Super Admin can delete users.");
            return;
        }

        // Verify target user is a regular user (not admin or super admin)
        if (user.getRole().contains("ROLE_ADMIN") || user.getRole().contains("ROLE_SUPER_ADMIN")) {
            showAlert(Alert.AlertType.ERROR, "Operation Not Allowed", "Cannot delete admin or super admin users.");
            return;
        }

        // Confirm delete action
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete User Confirmation");
        confirmAlert.setHeaderText("Delete User Account");
        confirmAlert.setContentText("Are you sure you want to permanently delete " + user.getName() + "'s account?\n\nThis action cannot be undone!");
        
        // Style the confirmation dialog to emphasize the danger
        confirmAlert.getDialogPane().setStyle("-fx-background-color: #ffe6e6;");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    // Delete user from database
                    userService.deleteUser(user.getId());
                    
                    loadUsers(); // Refresh the list
                    showAlert(Alert.AlertType.INFORMATION, "Success", 
                            "User " + user.getName() + " has been deleted successfully!");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", 
                            "Error deleting user: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }
}
