package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe utilitaire pour mettre à jour la structure de la base de données
 */
public class DatabaseUpdater {

    private static DatabaseUpdater instance;
    private Connection connection;

    private DatabaseUpdater() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public static DatabaseUpdater getInstance() {
        if (instance == null) {
            instance = new DatabaseUpdater();
        }
        return instance;
    }

    /**
     * Exécute un script SQL à partir d'un fichier
     * @param filePath Chemin du fichier SQL
     * @return true si l'exécution a réussi, false sinon
     */
    public boolean executeSqlScript(String filePath) {
        try {
            // Lire le fichier SQL
            StringBuilder sqlScript = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Ignorer les commentaires
                    if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                        sqlScript.append(line);
                    }
                }
            }

            // Exécuter le script SQL
            try (Statement statement = connection.createStatement()) {
                String[] sqlCommands = sqlScript.toString().split(";");
                for (String sqlCommand : sqlCommands) {
                    if (!sqlCommand.trim().isEmpty()) {
                        System.out.println("Exécution de la commande SQL : " + sqlCommand);
                        try {
                            statement.execute(sqlCommand);
                        } catch (SQLException e) {
                            // Vérifier si l'erreur est due à une colonne déjà existante
                            if (e.getMessage().contains("Duplicate column") || e.getMessage().contains("already exists")) {
                                System.out.println("La colonne existe déjà, ignorant l'erreur.");
                            } else {
                                throw e; // Propager les autres erreurs
                            }
                        }
                    }
                }
                System.out.println("Script SQL exécuté avec succès : " + filePath);
                return true;
            }
        } catch (IOException | SQLException e) {
            System.err.println("Erreur lors de l'exécution du script SQL : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Met à jour la structure de la base de données
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateDatabaseStructure() {
        boolean success = true;

        // Exécuter le script pour ajouter la colonne secret_key
        success &= executeSqlScript("src/main/resources/sql/add_secret_key_column.sql");

        // Exécuter le script pour ajouter les colonnes de réinitialisation de mot de passe
        success &= executeSqlScript("src/main/resources/sql/add_reset_password_columns.sql");

        return success;
    }
}
