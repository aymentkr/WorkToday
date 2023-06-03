package com.gluonapplication.Service;

import com.gluonapplication.DrawerManager;
import com.gluonapplication.Model.DatabaseDTO;
import com.gluonapplication.config.Config;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class dbConnection {
    public enum UserRole {
        ADMIN,
        READER_WRITE,
        READER,
    }
    private UserRole userRole=null;

    private static boolean status = false;

    private static Connection connection;


    public dbConnection() {
    }

    public void connect(String username, String password) throws SQLException {
        try {
            // Load the SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            // Create a connection to the database
            String connectionUrl = String.format(
                    "jdbc:sqlserver://%s:1433;"
                            + "database=%s;"
                            + ";encrypt=true;trustServerCertificate=true;"
                    , Config.DB_ADDRESS, Config.DB_NAME, username, password);
            connection = DriverManager.getConnection(connectionUrl, username, password);
            // Set appropriate privileges based on user role
            userRole = GetUserRole(username);
            if (userRole != null) {
                // Set appropriate privileges based on user role
                switch (userRole) {
                    case ADMIN -> DrawerManager.setAdmin();
                    case READER_WRITE -> DrawerManager.setReadWriter();
                    case READER -> DrawerManager.setReader();
                }
                setStatus(true);
            } else {
                // Handle case where no user role was found for the given username
                DrawerManager.setDEFAULT();
                setStatus(false);
            }
        } catch (SQLException e) {
           throw e;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("Failed to connect to database");
        }
    }

    public List<DatabaseDTO> executeQuery(String query) {
        List<DatabaseDTO> resultList = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int colAmount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                List<String> singleRow = new ArrayList<>();
                for (int i = 1; i <= colAmount; i++) {
                    singleRow.add(rs.getString(i));
                }
                resultList.add(new DatabaseDTO(singleRow));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public List<String> getColumnsOfTable(String query) {
        List<String> columns = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                columns.add(rs.getMetaData().getColumnName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columns;
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection GetConn() {
        return connection;
    }

    public Boolean getStatus() {
        return status;
    }
    public void setStatus(boolean b) {
        status=b;
    }

    public UserRole GetUserRole(String username) throws SQLException {
        String query = "SELECT u.name as UserName, s.name as RoleName FROM sys.server_role_members m JOIN sys.server_principals s ON m.role_principal_id = s.principal_id JOIN sys.server_principals u ON m.member_principal_id = u.principal_id where u.name =?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        UserRole userRole = null;
        while (rs.next()) {
            String roleName = rs.getString("RoleName").toUpperCase();
            if (roleName.contains("ADMIN")) {
                userRole = UserRole.ADMIN;
                break;
            } else if (roleName.equals("DB_DATAWRITER")) {
                userRole = UserRole.READER_WRITE;
            } else if (roleName.equals("DB_DATAREADER")) {
                userRole = UserRole.READER;
            }
        }
        rs.close();
        statement.close();
        return userRole;
    }

    public void prepareTable(String query, TableView<DatabaseDTO> tableView) throws SQLException {
        List<String> cols = getColumnsOfTable(query);
        setTableHeader(tableView, cols);
        setCellValueFactories(cols, tableView);
    }
    private void setCellValueFactories(List<String> cols, TableView<?> tableView)
    {
        for (int i = 0; i < cols.size(); i++) {
            int finalI = i;
            TableColumn<DatabaseDTO, String> column = (TableColumn<DatabaseDTO, String>) tableView.getColumns().get(i);

            column.setCellValueFactory(c -> c.getValue().getRow(finalI));
        }
    }


    private void setTableHeader(TableView<DatabaseDTO> tableView, List<String> cols)
    {
        tableView.getColumns().clear();

        for (String col : cols)
        {
            TableColumn<DatabaseDTO, String> column = new TableColumn<>();
            column.setText(col);
            tableView.getColumns().add(column);
        }
    }
    public UserRole getUserRole(){
        return userRole;
    }
}