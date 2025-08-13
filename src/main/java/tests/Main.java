package tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DatabaseUpdater;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Essayer de charger le fichier FXML avec un chemin absolu
            File file = new File("src/main/resources/fxml/Login.fxml");
            if (file.exists()) {
                URL url = file.toURI().toURL();
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();

                // Configurer la scène
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.setTitle("Login");
                primaryStage.show();
            } else {
                System.err.println("Fichier FXML non trouvé: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la page de login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Mettre à jour la structure de la base de données avant de lancer l'application
        System.out.println("Mise à jour de la structure de la base de données...");
        boolean updated = DatabaseUpdater.getInstance().updateDatabaseStructure();
        if (updated) {
            System.out.println("Structure de la base de données mise à jour avec succès.");
        } else {
            System.err.println("Erreur lors de la mise à jour de la structure de la base de données.");
        }

        // Lancer l'application
        launch(args);
    }
}
