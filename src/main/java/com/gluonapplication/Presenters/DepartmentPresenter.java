package com.gluonapplication.Presenters;

import com.gluonapplication.MainApplication;
import com.gluonapplication.Model.DatabaseDTO;
import com.gluonapplication.Service.dbConnection;
import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.*;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.sql.*;
import java.util.List;

public class DepartmentPresenter {
    private final Connection conn = MainApplication.getConnection().GetConn();
    private final dbConnection connection = MainApplication.getConnection();
    private String DepartmentID;
    @FXML
    private TableView<DatabaseDTO> DepartmentTable;
    @FXML
    private TextField name;

    @FXML
    private View secondary;

    public DepartmentPresenter() throws SQLException {
    }


    public void initialize() {
        secondary.setShowTransitionFactory(BounceInRightTransition::new);
        
        FloatingActionButton fab = new FloatingActionButton(MaterialDesignIcon.INFO.text,
                e -> System.out.println("Info"));
        fab.showOn(secondary);
        
        secondary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Department");
                appBar.getActionItems().add(MaterialDesignIcon.FAVORITE.button(e ->
                        System.out.println("Favorite")));
            }
        });
        if (connection.getStatus()) {
            try {
                displayDepartmentTable();
                displayRowData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayDepartmentTable() throws SQLException {
        try {
            String query = "SELECT * FROM department";
            connection.prepareTable(query,DepartmentTable);
            List<DatabaseDTO> resultList = connection.executeQuery(query);
            ObservableList<DatabaseDTO> data = FXCollections.observableArrayList(resultList);
            DepartmentTable.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // handle the exception
        }
    }

    @FXML
    private void displayRowData() {
        DepartmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                DatabaseDTO selectedDepartment = (DatabaseDTO) newSelection;
                DepartmentID = selectedDepartment.getRow(0).get();
                name.setText(selectedDepartment.getRow(1).get());
            }
        });
    }

    @FXML
    private void handleAdd(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
            String NameStr = name.getText().trim();
            try {
                if (NameStr != "") {
                    String sql = "INSERT INTO department (department_name) VALUES (?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, NameStr);
                    stmt.executeUpdate();
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Department added successfully.");
                    alert.showAndWait();
                    displayDepartmentTable();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleEdit(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
            String NameStr = name.getText().trim();
            String sql = "UPDATE department SET department_name=? WHERE department_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // set parameter values
                stmt.setString(1, NameStr);
                stmt.setString(2, DepartmentID);

                // execute update
                stmt.executeUpdate();

                // show success message and clear text fields
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Department updated successfully");
                alert.showAndWait();

                displayDepartmentTable();
            } catch (SQLException e) {
                // show error message and log exception
                System.err.println("Error updating department: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDelete(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN) {
            try {
                // Prepare the delete statement
                String sql = "DELETE FROM department WHERE department_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, DepartmentID);

                // Execute the delete statement
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    name.setText("");
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Department deleted successfully.");
                    alert.showAndWait();
                    displayDepartmentTable();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "Error deleting Department: " + e.getMessage() );
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }
}

