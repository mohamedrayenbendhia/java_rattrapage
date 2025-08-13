package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe utilitaire pour gérer les scènes avec des tailles standardisées
 */
public class SceneManager {
    
    // Tailles standardisées pour toutes les fenêtres
    public static final double STANDARD_WIDTH = 1000.0;
    public static final double STANDARD_HEIGHT = 700.0;
    
    /**
     * Charge un fichier FXML et crée une scène avec la taille standardisée
     * @param fxmlPath Le chemin vers le fichier FXML
     * @return La scène créée
     * @throws IOException Si le fichier FXML ne peut pas être chargé
     */
    public static Scene createStandardScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        return new Scene(root, STANDARD_WIDTH, STANDARD_HEIGHT);
    }
    
    /**
     * Charge un fichier FXML et crée une scène avec une taille personnalisée
     * @param fxmlPath Le chemin vers le fichier FXML
     * @param width La largeur de la scène
     * @param height La hauteur de la scène
     * @return La scène créée
     * @throws IOException Si le fichier FXML ne peut pas être chargé
     */
    public static Scene createScene(String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        return new Scene(root, width, height);
    }
    
    /**
     * Navigue vers une nouvelle page avec la taille standardisée
     * @param stage La fenêtre actuelle
     * @param fxmlPath Le chemin vers le fichier FXML
     * @param title Le titre de la fenêtre
     * @throws IOException Si le fichier FXML ne peut pas être chargé
     */
    public static void navigateToPage(Stage stage, String fxmlPath, String title) throws IOException {
        Scene scene = createStandardScene(fxmlPath);
        stage.setScene(scene);
        stage.setTitle(title);
        
        // Empêcher le redimensionnement de la fenêtre
        stage.setResizable(false);
        
        // Centrer la fenêtre
        stage.centerOnScreen();
        
        stage.show();
    }
    
    /**
     * Navigue vers une nouvelle page avec une taille personnalisée
     * @param stage La fenêtre actuelle
     * @param fxmlPath Le chemin vers le fichier FXML
     * @param title Le titre de la fenêtre
     * @param width La largeur de la fenêtre
     * @param height La hauteur de la fenêtre
     * @throws IOException Si le fichier FXML ne peut pas être chargé
     */
    public static void navigateToPage(Stage stage, String fxmlPath, String title, double width, double height) throws IOException {
        Scene scene = createScene(fxmlPath, width, height);
        stage.setScene(scene);
        stage.setTitle(title);
        
        // Empêcher le redimensionnement de la fenêtre
        stage.setResizable(false);
        
        // Centrer la fenêtre
        stage.centerOnScreen();
        
        stage.show();
    }
    
    /**
     * Configure une fenêtre avec les paramètres standards
     * @param stage La fenêtre à configurer
     */
    public static void configureStandardStage(Stage stage) {
        stage.setResizable(false);
        stage.setWidth(STANDARD_WIDTH);
        stage.setHeight(STANDARD_HEIGHT);
        stage.centerOnScreen();
    }
}
