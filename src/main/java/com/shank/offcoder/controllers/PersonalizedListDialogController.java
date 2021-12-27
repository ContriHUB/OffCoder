/*
 * Copyright (c) 2021, Shashank Verma <shashank.verma2002@gmail.com>(shank03)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.shank.offcoder.controllers;

import com.shank.offcoder.Launcher;
import com.shank.offcoder.app.PersonalizedListManager;
import com.shank.offcoder.cf.ProblemParser;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class PersonalizedListDialogController {

    private List<ProblemParser.Problem> list;

    public void passList(List<ProblemParser.Problem> list) {
        this.list = list;
    }

    @FXML
    private Button addBtn;

    @FXML
    private TextField newListField;

    @FXML
    private ChoiceBox<String> listChoiceBox;

    @FXML
    private void initialize() {
        List<String> arr = PersonalizedListManager.getListsNames();
        if (arr.isEmpty()) {
            listChoiceBox.setDisable(true);
            addBtn.setDisable(true);
        } else {
            listChoiceBox.setItems(FXCollections.observableArrayList(arr));
            listChoiceBox.setValue(arr.get(0));
        }
    }

    @FXML
    protected void addToList(ActionEvent event) {
        String listName = listChoiceBox.getSelectionModel().getSelectedItem().trim();
        PersonalizedListManager.addToList(listName, list);
        dismissDialog(event, listName);
    }

    @FXML
    protected void createList(ActionEvent event) {
        String listName = newListField.getText().trim();
        PersonalizedListManager.createList(listName, list);
        dismissDialog(event, listName);
    }

    private void dismissDialog(ActionEvent event, String listName) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Message");
        dialog.setHeaderText(null);
        dialog.setContentText("Added problem(s) to " + listName);
        dialog.initOwner(Launcher.get().mStage);
        dialog.showAndWait();

        Node source = (Node) event.getSource();
        ((Stage) source.getScene().getWindow()).close();
    }
}
