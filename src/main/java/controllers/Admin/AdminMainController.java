package controllers.Admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminMainController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private AdminSidebarController sidebarController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Passer la référence du BorderPane au contrôleur de la sidebar
        sidebarController.setMainBorderPane(mainBorderPane);
        
        // Charger la page dashboard par défaut
        sidebarController.handleDashboardClick(null);
    }
}
