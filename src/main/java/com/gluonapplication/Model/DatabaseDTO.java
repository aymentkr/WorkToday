package com.gluonapplication.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;

public class DatabaseDTO
{
    List<String> row;

    public DatabaseDTO(List<String> row)
    {
        this.row = row;
    }

    public StringProperty getRow(int i)
    {
        return new SimpleStringProperty(row.get(i));
    }

}
