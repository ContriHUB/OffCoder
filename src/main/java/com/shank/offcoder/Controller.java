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

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class Controller {

    private boolean mLoginStarted = false;

    @FXML
    private TextField handleField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private AnchorPane welcomePane;

    @FXML
    private Label userWelcome;

    @FXML
    protected void loginUser() {
        if (mLoginStarted) return;
        mLoginStarted = true;

        String handle = handleField.getText(), password = passwordField.getText();
        if (!handle.isEmpty() && !password.isEmpty()) {
            System.out.println("Handle: " + handleField.getText());
            System.out.println("Got Password");

            String ret = Codeforces.login(handle, password);
            if (ret.equals("Login Failed") || ret.equals("Error")) {
                handleField.setText("");
                passwordField.setText("");
            } else {
                welcomePane.toFront();
                userWelcome.setText("Welcome " + ret + " !");
            }
        }
        mLoginStarted = false;
    }
}