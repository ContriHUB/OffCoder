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
import com.shank.offcoder.app.AppThreader;
import com.shank.offcoder.app.NetworkClient;
import com.shank.offcoder.cf.Codeforces;
import com.shank.offcoder.cf.ProblemParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class Controller {

    private final ProblemParser mProblemSetHandler = new ProblemParser();

    private boolean mStarted = false;

    @FXML
    private TextField handleField, difficultyTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberCheck;

    @FXML
    private ProgressIndicator loginProgress;

    @FXML
    private AnchorPane welcomePane;

    @FXML
    private BorderPane loginPane;

    @FXML
    private Label userWelcome;

    @FXML
    private ListView<ProblemParser.Problem> problemListView;

    @FXML
    private ProgressIndicator problemRetProgress;

    @FXML
    private void initialize() {
        if (!AppData.get().<Boolean>getData(AppData.AUTO_LOGIN_KEY, false)) loginPane.toFront();

        problemRetProgress.setVisible(false);
        loadPageIndicator.setVisible(false);
        loginProgress.setVisible(false);
        downloadProgress.setVisible(false);

        problemListView.setCellFactory(param -> new ProblemCell());
        problemListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        problemListView.setOnMouseClicked(event -> {
            List<ProblemParser.Problem> list = problemListView.getSelectionModel().getSelectedItems();
            quesDownloadBtn.setDisable(list.isEmpty());

            if (!list.isEmpty()) quesDownloadBtn.setDisable(questionsDownloaded(list));
            quesDownloadBtn.setText("Download " + list.size() + " Question" + (list.size() > 1 ? "s" : ""));
        });
        quesDownloadBtn.setDisable(true);

        difficultyTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 5) newValue = newValue.substring(0, 4);
            if (!newValue.matches("\\d*")) {
                difficultyTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        webView.getEngine().load("about:blank");
    }

    // ----------------- LOGIN / LOGOUT ----------------- //

    @FXML
    private Button applyRateBtn;

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

            loginProgress.setVisible(mStarted);
            attemptLogin(handle, password);
        }
    }

    public void attemptLogin(String handle, String password) {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            mStarted = false;
            loginProgress.setVisible(false);
            return;
        }
        if (handle.equals(AppData.NULL_STR) || password.equals(AppData.NULL_STR)) {
            mStarted = false;
            loginProgress.setVisible(false);
            return;
        }

        Codeforces.login(handle, password, ret -> Platform.runLater(() -> {
            if (ret.equals("Codeforces down")) {
                Alert dialog = new Alert(Alert.AlertType.ERROR);
                dialog.setTitle("Connection Error");
                dialog.setHeaderText(null);
                dialog.setContentText("Codeforces down");
                dialog.initOwner(Launcher.get().mStage);
                mStarted = false;
                loginProgress.setVisible(false);

                dialog.showAndWait();
                handleField.setText("");
                passwordField.setText("");
                return;
            }
            if (ret.equals("Login Failed") || ret.equals("Error")) {
                Alert dialog = new Alert(Alert.AlertType.ERROR);
                dialog.setTitle("Login Error");
                dialog.setHeaderText(null);
                dialog.setContentText("Invalid handle or password");
                dialog.initOwner(Launcher.get().mStage);
                loginProgress.setVisible(false);
                dialog.showAndWait();
                passwordField.setText("");
            } else {
                handleField.setText("");
                passwordField.setText("");
                Launcher.get().freeWindowSize();
                welcomePane.toFront();
                userWelcome.setText("Welcome " + ret + " !");

                AppThreader.delay(() -> mProblemSetHandler.get(data -> populateListView(data, false)), 150);
                if (rememberCheck.isSelected()) {
                    AppData app = AppData.get();
                    app.writeData(AppData.HANDLE_KEY, ret);
                    app.writeData(AppData.PASS_KEY, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
                    app.writeData(AppData.AUTO_LOGIN_KEY, true);
                }
            }
            mStarted = false;
            loginProgress.setVisible(false);
        }));
    }

    @FXML
    protected void logoutUser() {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        if (mStarted) return;
        mStarted = true;

        if (!AppData.get().getData(AppData.DOWNLOADED_QUES, new JSONArray()).isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Are you sure to logout ?");
            alert.setContentText("You will loose all the downloaded questions.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) attemptLogout();
            return;
        }
        attemptLogout();
    }

    private void attemptLogout() {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        Codeforces.logout(data -> {
            if (data) {
                Platform.runLater(() -> {
                    problemListView.getSelectionModel().clearSelection();
                    problemListView.getItems().clear();
                    mProblemSetHandler.reset();
                    AppData.get().clearData();
                    Launcher.get().limitWindowSize();
                    loginPane.toFront();
                });
            }
            mStarted = false;
        });
    }

    public void showNetworkErrDialog() {
        Platform.runLater(() -> {
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setTitle("Network Error");
            dialog.setHeaderText(null);
            dialog.setContentText("Couldn't connect codeforces");
            dialog.initOwner(Launcher.get().mStage);
            dialog.showAndWait();
        });
    }

    // ----------------- PROBLEM LIST VIEW ----------------- //

    @FXML
    private Label pageNoLabel;

    @FXML
    private Button prevPageBtn, nextPageBtn, quesDownloadBtn;

    @FXML
    private ProgressIndicator loadPageIndicator;

    @FXML
    private ProgressBar downloadProgress;

    @FXML
    protected void applyDifficulty() {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        problemRetProgress.setVisible(true);
        AppThreader.delay(() -> {
            prevPageBtn.setDisable(true);
            nextPageBtn.setDisable(false);
            pageNoLabel.setText("Page: 1");

            problemListView.getSelectionModel().clearSelection();
            mProblemSetHandler.changeDifficulty(Integer.parseInt(difficultyTextField.getText().trim()), data -> populateListView(data, true));
        }, 500);
    }

    @FXML
    protected void nextPage() {
        loadPageIndicator.setVisible(true);
        AppThreader.delay(() -> mProblemSetHandler.nextPage(data -> populateListView(data, false)), 250);
    }

    @FXML
    protected void prevPage() {
        loadPageIndicator.setVisible(true);
        AppThreader.delay(() -> mProblemSetHandler.prevPage(data -> populateListView(data, false)), 250);
    }

    private boolean wasPrevBtnDisabled, wasNextBtnDisabled;

    @FXML
    protected void downloadQuestions() {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        final List<ProblemParser.Problem> list = problemListView.getSelectionModel().getSelectedItems();
        if (list.isEmpty()) return;

        problemListView.setDisable(true);
        quesDownloadBtn.setDisable(true);
        applyRateBtn.setDisable(true);

        wasPrevBtnDisabled = prevPageBtn.isDisabled();
        prevPageBtn.setDisable(true);

        wasNextBtnDisabled = nextPageBtn.isDisabled();
        nextPageBtn.setDisable(true);
        downloadProgress.setVisible(true);
        _downloadQues(list, data -> Platform.runLater(() -> {
            problemListView.setDisable(false);
            applyRateBtn.setDisable(false);

            if (!wasNextBtnDisabled) nextPageBtn.setDisable(false);
            if (!wasPrevBtnDisabled) prevPageBtn.setDisable(false);
            downloadProgress.setVisible(false);
            if (data != 0) {
                Alert dialog = new Alert(Alert.AlertType.ERROR);
                dialog.setTitle("Network Error");
                dialog.setHeaderText(null);
                dialog.setContentText("Couldn't download all questions\nFailed " + data + " questions.");
                dialog.initOwner(Launcher.get().mStage);
                dialog.showAndWait();
            }
        }));
    }

    private void _downloadQues(final List<ProblemParser.Problem> list, AppThreader.EventListener<Integer> listener) {
        new Thread(() -> {
            JSONArray arr = AppData.get().getData(AppData.DOWNLOADED_QUES, new JSONArray());
            double counter = 0;
            int failedCount = 0;
            for (ProblemParser.Problem p : list) {
                ++counter;
                downloadProgress.setProgress(counter / list.size());
                if (arr.toString().contains(p.code)) continue;

                String html = ProblemParser.trimHTML(Codeforces.HOST + p.url, null);
                if (ProblemParser.hasError(Jsoup.parse(html))) {
                    failedCount++;
                    continue;
                }

                arr.put(new JSONObject().put(p.code, html));
            }
            AppData.get().writeData(AppData.DOWNLOADED_QUES, arr);
            listener.onEvent(failedCount);
        }).start();
    }

    private boolean questionDownloaded(String code) {
        JSONArray arr = AppData.get().getData(AppData.DOWNLOADED_QUES, new JSONArray());
        if (arr.isEmpty()) return false;

        for (Object obj : arr) {
            JSONObject jObj = (JSONObject) obj;
            if (jObj.has(code)) return true;
        }
        return false;
    }

    private boolean questionsDownloaded(List<ProblemParser.Problem> list) {
        JSONArray arr = AppData.get().getData(AppData.DOWNLOADED_QUES, new JSONArray());
        if (arr.isEmpty()) return false;

        int count = 0;
        for (ProblemParser.Problem p : list) {
            for (Object obj : arr) {
                JSONObject jObj = (JSONObject) obj;
                if (jObj.has(p.code)) {
                    count++;
                    break;
                }
            }
        }
        return count == list.size();
    }

    private void populateListView(List<ProblemParser.Problem> list, boolean diffChange) {
        if (NetworkClient.isNetworkNotConnected()) {
            showNetworkErrDialog();
            return;
        }
        Platform.runLater(() -> {
            problemRetProgress.setVisible(false);
            prevPageBtn.setDisable(mProblemSetHandler.getPage() == 1);
            boolean updated = listUpdated(list, problemListView.getItems());
            nextPageBtn.setDisable(!updated && !diffChange);
            pageNoLabel.setText("Page: " + (updated || diffChange ? mProblemSetHandler.getPage() : mProblemSetHandler.revertPage()));

            problemListView.getItems().setAll(list);
            loadPageIndicator.setVisible(false);
        });
    }

    private boolean listUpdated(List<ProblemParser.Problem> prev, List<ProblemParser.Problem> next) {
        if (prev.isEmpty() || next.isEmpty()) return true;
        if (prev.size() != next.size()) return true;
        for (int i = 0; i < prev.size(); i++) {
            if (!prev.get(i).equals(next.get(i))) return true;
        }
        return false;
    }

    // ----------------- PROBLEM VIEW PAGE ----------------- //

    @FXML
    private AnchorPane problemPane;

    @FXML
    private WebView webView;

    @FXML
    private Button singlePageDownloadBtn;

    @FXML
    protected void webBtnGoBack() {
        welcomePane.toFront();
        webView.getEngine().load("about:blank");
    }

    public void loadWebPage(String url, String code) {
        boolean downloaded = questionDownloaded(code);
        singlePageDownloadBtn.setDisable(downloaded);
        if (!downloaded) {
            if (NetworkClient.isNetworkNotConnected()) {
                ((Controller) Launcher.get().mFxmlLoader.getController()).showNetworkErrDialog();
                return;
            }
        }
        problemPane.toFront();
        Platform.runLater(() -> {
            webView.getEngine().setJavaScriptEnabled(true);
            webView.getEngine().loadContent(ProblemParser.trimHTML(url, code));
        });
    }
}