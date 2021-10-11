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

package com.shank.offcoder;

import com.shank.offcoder.app.AppData;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Controller {

    private boolean mLoginStarted = false;

    @FXML
    private TextField handleField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberCheck;

    @FXML
    private AnchorPane welcomePane;

    @FXML
    private Label userWelcome;

    @FXML
    protected void loginUser() {
        if (NetworkClient.isNetworkNotConnected()) {
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setTitle("Network Error");
            dialog.setHeaderText(null);
            dialog.setContentText("Couldn't connect codeforces");
            dialog.showAndWait();
            return;
        }
        if (mLoginStarted) return;
        mLoginStarted = true;

        String handle = handleField.getText(), password = passwordField.getText();
        if (!handle.isEmpty() && !password.isEmpty()) {
            System.out.println("Handle: " + handleField.getText());
            System.out.println("Got Password");

            attemptLogin(handle, password);
        }
        mLoginStarted = false;
    }

    public void attemptLogin(String handle, String password) {
        if (NetworkClient.isNetworkNotConnected()) {
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setTitle("Network Error");
            dialog.setHeaderText(null);
            dialog.setContentText("Couldn't connect codeforces");
            dialog.showAndWait();
            return;
        }
        if (handle.equals(AppData.NULL_STR) || password.equals(AppData.NULL_STR)) return;

        String ret = Codeforces.login(handle, password);
        if (ret.equals("Login Failed") || ret.equals("Error")) {
            handleField.setText("");
            passwordField.setText("");

            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setTitle("Login Error");
            dialog.setHeaderText(null);
            dialog.setContentText("Invalid handle or password");
            dialog.showAndWait();
        } else {
            welcomePane.toFront();
            userWelcome.setText("Welcome " + ret + " !");

            if (rememberCheck.isSelected()) {
                AppData app = AppData.get();
                app.writeData(AppData.HANDLE_KEY, ret);
                app.writeData(AppData.PASS_KEY, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
                app.writeData(AppData.AUTO_LOGIN_KEY, true);
            }
        }
    }
}