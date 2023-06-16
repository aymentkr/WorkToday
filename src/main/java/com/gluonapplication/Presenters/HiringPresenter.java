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

public class HiringPresenter {
    private final dbConnection connection = MainApplication.getConnection();
    @FXML
    private TableView<DatabaseDTO> EmpDepTable;

    @FXML
    private View hiring;

    public HiringPresenter() throws SQLException {
    }


    public void initialize() {
        hiring.setShowTransitionFactory(BounceInRightTransition::new);

        FloatingActionButton fab = new FloatingActionButton(MaterialDesignIcon.INFO.text,
                e -> System.out.println("Info"));
        fab.showOn(hiring);

        hiring.showingProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue && connection.getStatus()) {
                AppBar appBar = AppManager.getInstance().getAppBar();
                appBar.setNavIcon(MaterialDesignIcon.MENU.button(e ->
                        AppManager.getInstance().getDrawer().open()));
                appBar.setTitleText("Hiring");
                try {
                    displayDepEmpTable();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void displayDepEmpTable() throws SQLException {
        try {
            String query = "SELECT * FROM EmployeeDepartment";
            connection.prepareTable(query, EmpDepTable);
            List<DatabaseDTO> resultList = connection.executeQuery(query);
            ObservableList<DatabaseDTO> data = FXCollections.observableArrayList(resultList);
            EmpDepTable.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            // handle the exception
        }
    }
}




