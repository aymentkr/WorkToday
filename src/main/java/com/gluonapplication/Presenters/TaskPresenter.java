package com.gluonapplication.Presenters;

import com.gluonapplication.MainApplication;
import com.gluonapplication.Model.DatabaseDTO;
import com.gluonapplication.Service.dbConnection;
import com.gluonhq.charm.glisten.application.AppManager;
import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class TaskPresenter {
    private final Connection conn = MainApplication.getConnection().GetConn();
    private final dbConnection connection = MainApplication.getConnection();
    private String TaskID;
    @FXML
    private TableView<DatabaseDTO> TaskTable,AssignTable;
    @FXML
    private DatePicker date;
    @FXML
    private CheckBox status;
    @FXML
    private ComboBox name;
    @FXML
    private TextArea task;

    @FXML
    private View tertiary;

    public TaskPresenter() throws SQLException {
    }


    public void initialize() {
        tertiary.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Task");
                appBar.getActionItems().add(MaterialDesignIcon.SEARCH.button(e ->
                        System.out.println("Search")));
                if (connection.getStatus()) {
                    try {
                        displayEmployees();
                        displayTaskTable();
                        displayRowData();
                        displayAssignTable();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void displayAssignTable() {
        try {
            String query = "SELECT * FROM EmployeeTaskAssignments";
            connection.prepareTable(query,AssignTable);
            List<DatabaseDTO> resultList = connection.executeQuery(query);
            ObservableList<DatabaseDTO> data = FXCollections.observableArrayList(resultList);
            AssignTable.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // handle the exception
        }
    }

    private void displayEmployees() throws SQLException {
        name.getItems().removeAll(name.getItems());
        // Execute the query to retrieve department IDs from the department table
        String query = "SELECT name FROM employee";
        // Create a statement to execute SQL queries
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        // Populate the ComboBox with department IDs
        while (resultSet.next()) {
            String department_name =  resultSet.getString("name");
            name.getItems().add(department_name);
        }
    }

    private void displayTaskTable() {
        try {
            String query = "SELECT * FROM tasks";
            connection.prepareTable(query,TaskTable);
            List<DatabaseDTO> resultList = connection.executeQuery(query);
            ObservableList<DatabaseDTO> data = FXCollections.observableArrayList(resultList);
            TaskTable.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // handle the exception
        }
    }

    @FXML
    private void displayRowData() {
        TaskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                DatabaseDTO selectedDepartment = newSelection;
                TaskID = selectedDepartment.getRow(0).get();
                task.setText(selectedDepartment.getRow(1).get());
            }
        });
        AssignTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                DatabaseDTO selectedDepartment = newSelection;
                TaskID = selectedDepartment.getRow(1).get();
            }
        });
    }

    @FXML
    private void handleAdd(){
        if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
            try {
                String TaskStr = task.getText().trim();
                String EmployeeIDStr = getEmployeeID(name.getValue().toString());
                if (TaskStr != "" && date.getValue() != null && name.getValue() != null)  {
                    if (!checkDuplicateTasks(TaskStr)) {
                        String sql = "INSERT INTO tasks (task_name) VALUES (?)";
                        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        stmt.setString(1, TaskStr);
                        int affectedRows = stmt.executeUpdate();
                        if (affectedRows > 0) {
                            ResultSet generatedKeys = stmt.getGeneratedKeys();
                            if (generatedKeys.next())
                                TaskID = String.valueOf(generatedKeys.getLong(1));
                        }
                        displayTaskTable();
                    }
                    if (!checkDuplicateTaskAssignment(TaskID, EmployeeIDStr)) {
                        String sql2 = "INSERT INTO EmployeeTaskAssignments (employee_id,task_id,date_assigned, status) VALUES (?,?,?,?)";
                        PreparedStatement stmt2 = conn.prepareStatement(sql2);
                        stmt2.setString(1, EmployeeIDStr);
                        stmt2.setString(2, TaskID);
                        stmt2.setString(3, String.valueOf(Date.valueOf(date.getValue())));
                        if (status.isSelected())
                            stmt2.setString(4, "Completed");
                        else
                            stmt2.setString(4, "In Progress ..");
                        stmt2.executeUpdate();
                        Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Task " + TaskID + " New Assignment added successfully.");
                        alert.showAndWait();
                        displayAssignTable();
                    }
                } else {
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "Please check out all required fields");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }

    private boolean checkDuplicateTasks(String taskStr) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE task_name = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, taskStr);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);
            return count > 0;
        }
        return false;
    }

    private String getEmployeeID(String name) {
        // Retrieve the department ID based on the department name from the database
        String employeeId = null;
        try {
            String query = "SELECT employee_id FROM employee WHERE name = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                employeeId = resultSet.getString("employee_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
        return employeeId;
    }

    @FXML
    private void handleEdit(){
       if (connection.getUserRole() == dbConnection.UserRole.ADMIN || connection.getUserRole() == dbConnection.UserRole.READER_WRITE) {
           try {
           String TaskStr = task.getText().trim();
           if (!checkDuplicateTasks(TaskStr)) {
               String sql = "UPDATE tasks SET task_name=? WHERE task_id=?";
               PreparedStatement stmt = conn.prepareStatement(sql);
               stmt.setString(1, TaskStr);
               stmt.setString(2, TaskID);
               // execute update task
               stmt.executeUpdate();
               // show success message and clear text fields
               Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Task updated successfully");
               alert.showAndWait();
               displayTaskTable();
           }
           String EmployeeIDStr = getEmployeeID(name.getValue().toString());
           if (checkDuplicateTaskAssignment(TaskID,EmployeeIDStr)) {
               String sql2 = "UPDATE EmployeeTaskAssignments SET date_assigned=? , status=? WHERE task_id=? AND employee_id=?";
               PreparedStatement stmt2 = conn.prepareStatement(sql2);
               stmt2.setString(1, String.valueOf(date.getValue()));
               if (status.isSelected())
                   stmt2.setString(2, "Completed");
               else
                   stmt2.setString(2, "In Progress ..");
               stmt2.setString(3, TaskID);
               stmt2.setString(4, EmployeeIDStr);
               // execute update EmployeeTaskAssignments
               stmt2.executeUpdate();
               displayAssignTable();
           }
            } catch (SQLException e) {
                // show error message and log exception
                System.err.println("Error updating task: " + e.getMessage());
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
                String sql2 = "DELETE FROM EmployeeTaskAssignments WHERE task_id = ?";
                PreparedStatement stmt2 = conn.prepareStatement(sql2);
                stmt2.setString(1, TaskID);
                // Execute the delete statement
                int rowsDeleted2 = stmt2.executeUpdate();

                // Prepare the delete statement
                String sql = "DELETE FROM tasks WHERE task_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, TaskID);

                // Execute the delete statement
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0 && rowsDeleted2>0) {
                    task.setText("");
                    status.setSelected(false);
                    Alert alert = new Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Task deleted successfully.");
                    alert.showAndWait();
                }
                displayAssignTable();
                displayTaskTable();
            } catch (SQLException e) {
                Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "Error deleting Task: " + e.getMessage() );
                alert.showAndWait();
            }
        }
        else {
            Alert alert = new Alert(javafx.scene.control.Alert.AlertType.WARNING, "You don't have permission to add, edit or delete!");
            alert.showAndWait();
        }
    }
    private boolean checkDuplicateTaskAssignment(String taskId, String employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM EmployeeTaskAssignments WHERE task_id = ? AND employee_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, taskId);
        stmt.setString(2, employeeId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int count = rs.getInt(1);
            return count > 0;
        }
        return false;
    }
}

