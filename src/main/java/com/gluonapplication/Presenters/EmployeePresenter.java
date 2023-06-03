package com.gluonapplication.Presenters;

import com.gluonapplication.MainApplication;
import com.gluonapplication.Model.DatabaseDTO;
import com.gluonapplication.Service.dbConnection;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.TextField;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;

import java.sql.*;
import java.util.List;

public class EmployeePresenter {
    private final Connection conn = MainApplication.getConnection().GetConn();
    private final dbConnection connection = MainApplication.getConnection();
    String EmployeeID;

    @FXML
    private TableView<DatabaseDTO> EmployeesTable;

    @FXML
    private TextField name,age,phone_number,address,email;
    @FXML
    private ComboBox department;

    @FXML
    private View primary;

    public EmployeePresenter() throws SQLException {
    }


    public void initialize() {
        primary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> 
                        AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Employee");
                appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e -> 
                        System.out.println("Search")));
                if (connection.getStatus()) {
                    try {
                        displayDepartments();
                        displayEmployeesTable();
                        displayRowData();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void displayDepartments() throws SQLException {
        department.getItems().removeAll(department.getItems());
        // Execute the query to retrieve department IDs from the department table
        String query = "SELECT department_name FROM department";

        // Create a statement to execute SQL queries
        Statement statement = conn.createStatement();

        ResultSet resultSet = statement.executeQuery(query);

        // Populate the ComboBox with department IDs
        while (resultSet.next()) {
            String department_name =  resultSet.getString("department_name");
            department.getItems().add(department_name);
        }
    }


    private void displayEmployeesTable() throws SQLException {
        try {
            String query = "SELECT * FROM employee";
            connection.prepareTable(query,EmployeesTable);
            List<DatabaseDTO> resultList = connection.executeQuery(query);
            ObservableList<DatabaseDTO> data = FXCollections.observableArrayList(resultList);
            EmployeesTable.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // handle the exception
        }
    }

    private String getDepartmentId(String departmentName) {
        // Retrieve the department ID based on the department name from the database
        String departmentId = null;
        try {
            String query = "SELECT department_id FROM department WHERE department_name = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, departmentName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                departmentId = resultSet.getString("department_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
        return departmentId;
    }
    private String getDepartmentName(String departmentId) {
        // Retrieve the department ID based on the department name from the database
        String departmentName = null;
        try {
            String query = "SELECT department_name FROM department WHERE department_id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, departmentId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                departmentName = resultSet.getString("department_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
        return departmentName;
    }
    @FXML
    private void displayRowData() {
        EmployeesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                DatabaseDTO selectedEmployee = (DatabaseDTO) newSelection;
                name.setText(selectedEmployee.getRow(1).get());
                age.setText(selectedEmployee.getRow(2).get());
                phone_number.setText(selectedEmployee.getRow(4).get());
                address.setText(selectedEmployee.getRow(5).get());
                email.setText(selectedEmployee.getRow(6).get());

                // Set the selected department in the ComboBox
                department.getSelectionModel().select(getDepartmentName(selectedEmployee.getRow(3).get()));
                EmployeeID = selectedEmployee.getRow(0).get();
            }
        });
    }


    @FXML
    private void handleAdd(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
            String NameStr = name.getText().trim();
            String AgeStr = age.getText().trim();
            String AddressStr = address.getText().trim();
            String EmailStr = email.getText().trim();
            String PhoneNumberStr = phone_number.getText().trim();
            String DepartmentIdStr = getDepartmentId(department.getValue().toString());

            try {
                String sql = "INSERT INTO employee (name, age, address, phone_number, email, department_id) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, NameStr);
                stmt.setString(2, AgeStr);
                stmt.setString(3, AddressStr);
                stmt.setString(4, PhoneNumberStr);
                stmt.setString(5, EmailStr);
                stmt.setString(6, DepartmentIdStr);
                stmt.executeUpdate();
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Employee added successfully.");
                alert.showAndWait();
                displayEmployeesTable();
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
            String AgeStr = age.getText().trim();
            String AddressStr = address.getText().trim();
            String EmailStr = email.getText().trim();
            String PhoneNumberStr = phone_number.getText().trim();
            String DepartmentIdStr = getDepartmentId(department.getValue().toString());
            String sql = "UPDATE employee SET name=?, age=?, address=?, email=?, phone_number=?, department_id=? WHERE employee_id=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // set parameter values
                stmt.setString(1, NameStr);
                stmt.setString(2, AgeStr);
                stmt.setString(3, AddressStr);
                stmt.setString(4, EmailStr);
                stmt.setString(5, PhoneNumberStr);
                stmt.setString(6, DepartmentIdStr);
                stmt.setString(7, EmployeeID);

                // execute update
                stmt.executeUpdate();

                // show success message and clear text fields
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Employee updated successfully");
                alert.showAndWait();

                displayEmployeesTable();
            } catch (SQLException e) {
                // show error message and log exception
                System.err.println("Error updating customer: " + e.getMessage());
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
                String sql = "DELETE FROM employee WHERE employee_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, EmployeeID);

                // Execute the delete statement
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    name.setText("");
                    age.setText("");
                    address.setText("");
                    email.setText("");
                    phone_number.setText("");
                    department.getSelectionModel().select(0);
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Employee deleted successfully.");
                    alert.showAndWait();
                    displayEmployeesTable();
                }
            } catch (SQLException e) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "Error deleting Employee: " + e.getMessage() );
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }


}
