package com.gluonapplication.Presenters;

import com.gluonapplication.MainApplication;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;

import java.sql.SQLException;

public class LoginPresenter {

    @FXML
    private View loginView ;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    // Inject the AppManager instance



    public void initialize() {
        loginView.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Login");
            }
        });
    }

    public void handleButtonLogin(ActionEvent event){
        try {
            MainApplication.getConnection().connect(usernameField.getText(), passwordField.getText());
            Alert alert = new Alert(AlertType.INFORMATION, "Successfully connected!");
            alert.showAndWait();
            AppManager.getInstance().switchView(MainApplication.EMPLOYEE_VIEW);

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR, "Connection failed!, please try again");
            alert.showAndWait();
        }
    }
}
