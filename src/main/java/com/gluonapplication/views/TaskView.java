package com.gluonapplication.views;

import com.gluonhq.charm.glisten.mvc.View;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.Objects;

public class TaskView {
    public View getView() {
        try {
            return FXMLLoader.load(Objects.requireNonNull(EmployeeView.class.getResource("task.fxml")));
        } catch (IOException e) {
            System.out.println("IOException: " + e);
            return new View();
        }
    }
}
