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
import com.shank.offcoder.app.AppData;
import com.shank.offcoder.app.Coroutine;
import com.shank.offcoder.app.NetworkClient;
import com.shank.offcoder.cf.Codeforces;
import com.shank.offcoder.cf.ProblemSetHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class Controller {

    private final ProblemSetHandler mProblemSetHandler = new ProblemSetHandler();

    private boolean mStarted = false;

    @FXML
    private TextField handleField, difficultyTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberCheck;

    @FXML
    private AnchorPane welcomePane;

    @FXML
    private BorderPane loginPane;

    @FXML
    private Label userWelcome;

    @FXML
    private ListView<ProblemSetHandler.Problem> problemListView;

    @FXML
    private ProgressIndicator problemRetProgress;

    @FXML
    private void initialize() {
        if (!AppData.get().<Boolean>getData(AppData.AUTO_LOGIN_KEY, false)) loginPane.toFront();
        problemRetProgress.setVisible(false);
        loadPageIndicator.setVisible(false);
        problemListView.setCellFactory(param -> new ProblemCell());
        difficultyTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 5) newValue = newValue.substring(0, 4);
            if (!newValue.matches("\\d*")) {
                difficultyTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    // ----------------- LOGIN / LOGOUT ----------------- //

    @FXML
    protected void loginUser() {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        if (mStarted) return;
        mStarted = true;

        String handle = handleField.getText().trim(), password = passwordField.getText().trim();
        if (!handle.isEmpty() && !password.isEmpty()) {
            System.out.println("Handle: " + handleField.getText().trim());
            System.out.println("Got Password");

            attemptLogin(handle, password);
        }
        mStarted = false;
    }

    public void attemptLogin(String handle, String password) {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        if (handle.equals(AppData.NULL_STR) || password.equals(AppData.NULL_STR)) return;

        String ret = Codeforces.login(handle, password);
        if (ret.equals("Login Failed") || ret.equals("Error")) {
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setTitle("Login Error");
            dialog.setHeaderText(null);
            dialog.setContentText("Invalid handle or password");
            dialog.initOwner(Launcher.get().mStage);
            dialog.showAndWait();
        } else {
            Launcher.get().freeWindowSize();
            welcomePane.toFront();
            userWelcome.setText("Welcome " + ret + " !");

            new Coroutine<Void>().delay(() -> {
                populateListView(mProblemSetHandler.get(), false);
                return null;
            }, 150);
            if (rememberCheck.isSelected()) {
                AppData app = AppData.get();
                app.writeData(AppData.HANDLE_KEY, ret);
                app.writeData(AppData.PASS_KEY, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
                app.writeData(AppData.AUTO_LOGIN_KEY, true);
            }
        }
        handleField.setText("");
        passwordField.setText("");
    }

    @FXML
    protected void logoutUser() {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        if (mStarted) return;
        mStarted = true;
        if (Codeforces.logout()) {
            problemListView.getSelectionModel().clearSelection();
            problemListView.getItems().clear();
            mProblemSetHandler.reset();
            removeAutoLogin();
            Launcher.get().limitWindowSize();
            loginPane.toFront();
        }
        mStarted = false;
    }

    private void removeAutoLogin() {
        AppData appData = AppData.get();
        appData.writeData(AppData.AUTO_LOGIN_KEY, false);
        appData.writeData(AppData.HANDLE_KEY, AppData.NULL_STR);
        appData.writeData(AppData.PASS_KEY, AppData.NULL_STR);
    }

    private void showNetworkErrDialog() {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("Network Error");
        dialog.setHeaderText(null);
        dialog.setContentText("Couldn't connect codeforces");
        dialog.initOwner(Launcher.get().mStage);
        dialog.showAndWait();
    }

    // ----------------- PROBLEM LIST VIEW ----------------- //

    @FXML
    private Label pageNoLabel;

    @FXML
    private Button prevPageBtn, nextPageBtn;

    @FXML
    private ProgressIndicator loadPageIndicator;

    @FXML
    protected void applyDifficulty() {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        problemRetProgress.setVisible(true);
        new Coroutine<Void>().delay(() -> {
            prevPageBtn.setDisable(true);
            nextPageBtn.setDisable(false);
            pageNoLabel.setText("Page: 1");

            problemListView.getSelectionModel().clearSelection();
            populateListView(mProblemSetHandler.changeDifficulty(Integer.parseInt(difficultyTextField.getText().trim())), true);
            return null;
        }, 500);
    }

    @FXML
    protected void nextPage() {
        loadPageIndicator.setVisible(true);
        new Coroutine<Void>().delay(() -> {
            populateListView(mProblemSetHandler.nextPage(), false);
            return null;
        }, 500);
    }

    @FXML
    protected void prevPage() {
        loadPageIndicator.setVisible(true);
        new Coroutine<Void>().delay(() -> {
            populateListView(mProblemSetHandler.prevPage(), false);
            return null;
        }, 500);
    }

    private void populateListView(List<ProblemSetHandler.Problem> list, boolean diffChange) {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        problemRetProgress.setVisible(false);
        prevPageBtn.setDisable(mProblemSetHandler.getPage() == 1);
        boolean updated = listUpdated(list, problemListView.getItems());
        nextPageBtn.setDisable(!updated && !diffChange);
        pageNoLabel.setText("Page: " + (updated || diffChange ? mProblemSetHandler.getPage() : mProblemSetHandler.revertPage()));

        problemListView.getItems().setAll(list);
        loadPageIndicator.setVisible(false);
    }

    private boolean listUpdated(List<ProblemSetHandler.Problem> prev, List<ProblemSetHandler.Problem> next) {
        if (prev.isEmpty() || next.isEmpty()) return true;
        if (prev.size() != next.size()) return true;
        for (int i = 0; i < prev.size(); i++) {
            if (!prev.get(i).equals(next.get(i))) return true;
        }
        return false;
    }
}