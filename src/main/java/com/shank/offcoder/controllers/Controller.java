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
import com.shank.offcoder.app.PersonalizedListManager;
import com.shank.offcoder.cf.*;
import com.shank.offcoder.cli.CompilerManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Controller {

    private final ProblemParser mProblemSetHandler = new ProblemParser();
    private final CompilerManager mCompilerManager = CompilerManager.getInstance();
    @FXML
    public ProgressBar downloadProgress;
    private SampleCompilationTests mCompilation = new SampleCompilationTests();
    private ProblemParser.Problem mProblem = null;
    private boolean mStarted = false, mShowingDownloaded = false;
    @FXML
    private TextField handleField, difficultyTextField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberCheck;
    @FXML
    private ProgressIndicator loginProgress;
    @FXML
    private AnchorPane welcomePane, personalizedListPane;
    @FXML
    private BorderPane loginPane;
    @FXML
    private Label userWelcome, splashText;
    @FXML
    private ListView<ProblemParser.Problem> problemListView;
    @FXML
    private ProgressIndicator problemRetProgress;
    @FXML
    private Button retryBtn;

    // ----------------- LOGIN / LOGOUT ----------------- //
    @FXML
    private Button applyRateBtn;
    @FXML
    private Label pageNoLabel;
    @FXML
    private Button prevPageBtn, nextPageBtn, quesDownloadBtn, downloadedBtn,
            personalizedListBtn, browseQuesBtn, codeSearchBtn, addToListBtn;
    @FXML
    private Label queueLabel;
    @FXML
    private ProgressIndicator loadPageIndicator;
    @FXML
    private TextField codeTextField;
    private boolean wasPrevBtnDisabled, wasNextBtnDisabled;

    // ----------------- PROBLEM LIST VIEW ----------------- //
    @FXML
    private AnchorPane problemPane;
    @FXML
    private WebView webView;
    @FXML
    private Button compileBtn, submitBtn;
    @FXML
    private Label acceptedLabel, selectedFile;
    @FXML
    private ChoiceBox<String> langSelector;
    @FXML
    private ListView<Codeforces.PreviousSubmission> prevSubListView;
    @FXML
    private AnchorPane prevSubPane;
    @FXML
    private Button prevSubBtn;
    @FXML
    private ListView<String> listNameListView;
    @FXML
    private ListView<ProblemParser.Problem> listProblemListView;

    /**
     * Main UI method to initialize views
     * with their default settings.
     */
    @FXML
    private void initialize() {
        if (!AppData.get().<Boolean>getData(AppData.AUTO_LOGIN_KEY, false)) loginPane.toFront();

        problemRetProgress.setVisible(false);
        loadPageIndicator.setVisible(false);
        loginProgress.setVisible(false);
        downloadProgress.setVisible(false);
        retryBtn.setVisible(false);
        acceptedLabel.setVisible(false);
        compileBtn.setDisable(true);
        submitBtn.setDisable(true);

        prevSubListView.setCellFactory(para -> new SubmissionCell());
        problemListView.setCellFactory(param -> new ProblemCell());
        problemListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        problemListView.setOnMouseClicked(event -> {
            List<ProblemParser.Problem> list = problemListView.getSelectionModel().getSelectedItems();
            quesDownloadBtn.setDisable(list.isEmpty());

            if (!list.isEmpty()) quesDownloadBtn.setDisable(DownloadManager.areQuestionsDownloaded(list));
            quesDownloadBtn.setText("Download " + list.size() + " Question" + (list.size() > 1 ? "s" : ""));
            addToListBtn.setDisable(problemListView.getSelectionModel().isEmpty());
        });
        listProblemListView.setCellFactory(param -> new ProblemCell());
        listProblemListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listNameListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        quesDownloadBtn.setDisable(true);

        difficultyTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 5) newValue = newValue.substring(0, 4);
            if (!newValue.matches("\\d*")) {
                difficultyTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        webView.getEngine().load("about:blank");

        langSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected: " + newValue);
            String loadedExt = mCompilation.getExt(), expectedExt = Codeforces.getLangExt(newValue);
            compileBtn.setDisable(!loadedExt.equals(expectedExt));
            submitBtn.setDisable(!loadedExt.equals(expectedExt));
        });
    }

    /**
     * Function to log in user from Login Page
     * <p>
     * This method checks input and network connect then executes
     * {@link #attemptLogin(String, String, boolean)}
     */
    @FXML
    protected void loginUser() {
        NetworkClient.withNetwork(__ -> {
            if (mStarted) return;
            mStarted = true;

            String handle = handleField.getText().trim(), password = passwordField.getText().trim();
            if (!handle.isEmpty() && !password.isEmpty()) {
                System.out.println("Handle: " + handleField.getText().trim());
                System.out.println("Got Password");

                loginProgress.setVisible(mStarted);
                attemptLogin(handle, password, false);
            }
        }, null);
    }

    /**
     * In case of network error, this method is called
     */
    @FXML
    protected void reAttemptLogin() {
        retryBtn.setVisible(false);
        splashText.setText("Loading ...");
        attemptLogin(AppData.get().getData(AppData.HANDLE_KEY, AppData.NULL_STR), new String(Base64.getDecoder().decode(AppData.get().getData(AppData.PASS_KEY, AppData.NULL_STR))), true);
    }

    /**
     * The main method of logging in;
     * {@link Codeforces#login(String, String, AppThreader.EventCallback)}
     */
    public void attemptLogin(String handle, String password, boolean auto) {
        NetworkClient.withNetwork(__ -> {
            if (handle.equals(AppData.NULL_STR) || password.equals(AppData.NULL_STR)) {
                mStarted = false;
                loginProgress.setVisible(false);
                return;
            }

            Codeforces.login(handle, password, ret -> Platform.runLater(() -> {
                if (ret.equals("Codeforces down")) {
                    mStarted = false;
                    Alert dialog = new Alert(Alert.AlertType.ERROR);
                    dialog.setTitle("Connection Error");
                    dialog.setHeaderText(null);
                    dialog.setContentText("Codeforces down");
                    dialog.initOwner(Launcher.get().mStage);
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
        }, __ -> {
            mStarted = false;
            if (auto) {
                splashText.setText("Couldn't connect codeforces");
                retryBtn.setVisible(true);
            } else {
                showNetworkErrDialog();
                loginProgress.setVisible(false);
            }
        });
    }

    /**
     * Method to logout
     */
    @FXML
    protected void logoutUser() {
        NetworkClient.withNetwork(__ -> {
            if (mStarted) return;
            mStarted = true;

            if (!AppData.get().getData(AppData.DOWNLOADED_QUES, new JSONArray()).isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Are you sure to logout ?");
                alert.setContentText("You will loose all the downloaded questions.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) attemptLogout();
                return;
            }
            attemptLogout();
        }, null);
    }

    /**
     * The main method of logout;
     * {@link Codeforces#logout(AppThreader.EventCallback)}
     */
    private void attemptLogout() {
        NetworkClient.withNetwork(__ -> Codeforces.logout(data -> {
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
        }), null);
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

    /**
     * Method to update the "Queue" label on home page
     */
    public void updateQueueLab(String updated) {
        queueLabel.setText(updated);
    }

    // ----------------- PROBLEM VIEW PAGE ----------------- //

    /**
     * Method to fetch problems according to rating (difficulty)
     */
    @FXML
    protected void applyDifficulty() {
        NetworkClient.withNetwork(__ -> {
            problemRetProgress.setVisible(true);
            AppThreader.delay(() -> {
                prevPageBtn.setDisable(true);
                nextPageBtn.setDisable(false);
                pageNoLabel.setText("Page: 1");

                if (mShowingDownloaded) filterDownloaded();

                problemListView.getSelectionModel().clearSelection();
                mProblemSetHandler.changeDifficulty(Integer.parseInt(difficultyTextField.getText().trim()), data -> populateListView(data, true));
            }, 500);
        }, null);
    }

    @FXML
    protected void onBrowseBtnClicked() {
        downloadedBtn.setDisable(true);
        if (mShowingDownloaded) {
            filterDownloaded();
            return;
        }
        mProblemSetHandler.get(data -> populateListView(data, false));
    }

    /**
     * This function will load question through problem code
     */
    @FXML
    protected void searchProblemCode() {
        NetworkClient.withNetwork(__ -> {
            String problemCode = codeTextField.getText().trim();
            if (problemCode.isEmpty()) return;

            problemCode = problemCode.toUpperCase(Locale.ROOT).replace("/", "");
            String url = "/problemset/problem/" + problemCode.substring(0, problemCode.length() - 1) + "/" + problemCode.charAt(problemCode.length() - 1);
            ProblemParser.Problem pr = new ProblemParser.Problem(problemCode, "", url, "", false);

            loadWebPage(pr);
        }, null);
    }

    @FXML
    protected void addQuesToList() {
        List<ProblemParser.Problem> list = problemListView.getSelectionModel().getSelectedItems();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("personalized_list_dialog_view.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 500, 250);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);

            PersonalizedListDialogController dialogController = fxmlLoader.getController();
            dialogController.passList(list);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * Method that download questions and handle UI as well.
     * calls: {@link DownloadManager#downloadQuestion(Controller, List, AppThreader.EventCallback)}
     */
    @FXML
    protected void downloadQuestions() {
        NetworkClient.withNetwork(__ -> {
            final List<ProblemParser.Problem> list = problemListView.getSelectionModel().getSelectedItems();
            if (list.isEmpty()) return;

            prevSubBtn.setDisable(true);
            problemListView.setDisable(true);
            quesDownloadBtn.setDisable(true);
            applyRateBtn.setDisable(true);
            downloadedBtn.setDisable(true);
            personalizedListBtn.setDisable(true);
            browseQuesBtn.setDisable(true);
            codeSearchBtn.setDisable(true);
            addToListBtn.setDisable(true);

            wasPrevBtnDisabled = prevPageBtn.isDisabled();
            prevPageBtn.setDisable(true);

            wasNextBtnDisabled = nextPageBtn.isDisabled();
            nextPageBtn.setDisable(true);
            downloadProgress.setVisible(true);
            DownloadManager.downloadQuestion(this, list, data -> Platform.runLater(() -> {
                prevSubBtn.setDisable(false);
                problemListView.setDisable(false);
                applyRateBtn.setDisable(false);
                downloadedBtn.setDisable(false);
                personalizedListBtn.setDisable(false);
                browseQuesBtn.setDisable(false);
                codeSearchBtn.setDisable(false);
                addToListBtn.setDisable(problemListView.getSelectionModel().isEmpty());

                if (!wasNextBtnDisabled) nextPageBtn.setDisable(false);
                if (!wasPrevBtnDisabled) prevPageBtn.setDisable(false);
                downloadProgress.setVisible(false);
                problemListView.refresh();
                if (data != 0) {
                    Alert dialog = new Alert(Alert.AlertType.ERROR);
                    dialog.setTitle("Network Error");
                    dialog.setHeaderText(null);
                    dialog.setContentText("Couldn't download all questions\nFailed " + data + " questions.");
                    dialog.initOwner(Launcher.get().mStage);
                    dialog.showAndWait();
                }
            }));
        }, null);
    }

    /**
     * Method to show the list of problems on UI
     *
     * @param list       The list of problems
     * @param diffChange The page number will change if this is `false`
     *                   else it will reset to "Page 1"
     */
    private void populateListView(List<ProblemParser.Problem> list, boolean diffChange) {
        NetworkClient.withNetwork(__ -> {
            problemRetProgress.setVisible(false);
            prevPageBtn.setDisable(mProblemSetHandler.getPage() == 1);
            boolean updated = isListUpdated(list, problemListView.getItems());
            nextPageBtn.setDisable(!updated && !diffChange);
            pageNoLabel.setText("Page: " + (updated || diffChange ? mProblemSetHandler.getPage() : mProblemSetHandler.revertPage()));

            problemListView.getItems().setAll(list);
            loadPageIndicator.setVisible(false);
        }, null);
    }

    /**
     * @return whether the list of problems have updated
     */
    private boolean isListUpdated(List<ProblemParser.Problem> prev, List<ProblemParser.Problem> next) {
        if (prev.isEmpty() || next.isEmpty()) return true;
        if (prev.size() != next.size()) return true;
        for (int i = 0; i < prev.size(); i++) {
            if (!prev.get(i).equals(next.get(i))) return true;
        }
        return false;
    }

    /**
     * Method to toggle showing of all the downloaded questions.
     */
    @FXML
    protected void filterDownloaded() {
        if (!mShowingDownloaded) {
            mShowingDownloaded = true;
            downloadedBtn.setText("Question Lists");

            JSONArray probArr = AppData.get().getData(AppData.DOWNLOADED_QUES, new JSONArray());
            List<ProblemParser.Problem> list = new ArrayList<>();
            for (Object obj : probArr) {
                JSONObject jObj = (JSONObject) obj;
                list.add(new ProblemParser.Problem(jObj.getString(AppData.P_CODE_KEY), jObj.getString(AppData.P_NAME_KEY), jObj.getString(AppData.P_URL_KEY), jObj.getString(AppData.P_RATING_KEY), jObj.getBoolean(AppData.P_ACCEPTED_KEY)));
            }
            populateListView(list, false);
            quesDownloadBtn.setDisable(true);
            return;
        }
        NetworkClient.withNetwork(__ -> {
            problemRetProgress.setVisible(true);
            mShowingDownloaded = false;
            downloadedBtn.setText("Downloaded Questions");
            AppThreader.delay(() -> {
                mProblemSetHandler.get(data -> populateListView(data, false));
                problemRetProgress.setVisible(false);
                quesDownloadBtn.setDisable(false);
            }, 250);
        }, null);
    }

    /**
     * Method to go back to problem list view from question view
     */
    @FXML
    protected void webBtnGoBack() {
        mProblem = null;
        mCompilation = new SampleCompilationTests();
        welcomePane.toFront();
        acceptedLabel.setVisible(false);
        webView.getEngine().load("about:blank");
    }

    // ----------------- PREVIOUS SUBMISSION ----------------- //

    /**
     * Method to select file from system
     * for testing test cases.
     */
    @FXML
    protected void selectFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select source code file");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Source File", "*.cpp", "*.c", "*.py", "*.java"));
        File sourceCodeFile = chooser.showOpenDialog(Launcher.get().mStage);
        if (sourceCodeFile != null && mCompilation.setSourceFile(sourceCodeFile)) {
            selectedFile.setText(sourceCodeFile.getName());

            String loadedExt = mCompilation.getExt(), expectedExt = Codeforces.getLangExt(langSelector.getSelectionModel().getSelectedItem());
            compileBtn.setDisable(!loadedExt.equals(expectedExt));
            submitBtn.setDisable(!loadedExt.equals(expectedExt));
        }
    }

    /**
     * Method that compiles, test and
     * give verdict on sample test cases
     */
    @FXML
    protected void compileTest() {
        mCompilation.compile(langSelector.getSelectionModel().getSelectedItem());
    }

    /**
     * Method to submit code.
     * <p>
     * This will queue the submission in {@link SubmissionQueue}
     * and {@link SubmissionQueue} will submit the code when connected
     * to network.
     */
    @FXML
    protected void submitCode() {
        if (mProblem == null) return;
        NetworkClient.withNetwork(__ -> {
            Alert alert = new Alert(Alert.AlertType.NONE, "Uploading ...");
            alert.setTitle("Submitting");
            alert.getButtonTypes().addAll(ButtonType.OK);
            alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
            alert.initOwner(Launcher.get().mStage);
            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
            alert.setOnShown(e -> Codeforces.submitCode(new Codeforces.Submission(langSelector.getSelectionModel().getSelectedItem(), mCompilation.getSourceCode(), mProblem),
                    data -> Platform.runLater(() -> {
                        alert.close();
                        showSubmitDialog(data);
                    })));
            alert.show();
        }, __ -> SubmissionQueue.get().queue(new Codeforces.Submission(langSelector.getSelectionModel().getSelectedItem(), mCompilation.getSourceCode(), mProblem), this::showSubmitDialog));
    }

    private void showSubmitDialog(SubmissionQueue.PostResult data) {
        Alert infoAlert;
        if (data.submitted) {
            infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Submission");
            infoAlert.setContentText("Problem " + data.code + " submitted successfully");
        } else {
            infoAlert = new Alert(Alert.AlertType.ERROR);
            infoAlert.setTitle("Submission error");
            infoAlert.setContentText("Could not submit " + data.code);
        }
        infoAlert.showAndWait();
    }

    /**
     * Method that loads the problem HTML
     */
    public void loadWebPage(ProblemParser.Problem pr) {
        mCompilation = new SampleCompilationTests();
        mProblem = pr;
        compileBtn.setDisable(true);
        submitBtn.setDisable(true);
        selectedFile.setText("<No file selected>");

        boolean downloaded = DownloadManager.isQuestionDownloaded(pr.code);
        if (!downloaded) {
            if (NetworkClient.isNetworkNotConnected()) {
                ((Controller) Launcher.get().mFxmlLoader.getController()).showNetworkErrDialog();
                return;
            }
        }
        problemPane.toFront();
        if (pr.accepted) acceptedLabel.setVisible(true);
        Platform.runLater(() -> {
            webView.getEngine().setJavaScriptEnabled(true);
            String html = ProblemParser.trimHTML(ProblemParser.getQuestion(Codeforces.HOST + pr.url, pr.code));
            webView.getEngine().loadContent(html);
            mCompilation.setDoc(html);

            String selectedItem = langSelector.getSelectionModel().getSelectedItem();
            langSelector.setItems(FXCollections.observableArrayList(mCompilerManager.getLanguageList()));
            langSelector.setValue(selectedItem);
        });
    }

    // -------------------- PERSONALIZED LISTS --------------------- //

    @FXML
    protected void goToPrevSub() {
        NetworkClient.withNetwork(__ -> {
            prevSubPane.toFront();
            Codeforces.getPreviousSubmission(data -> Platform.runLater(() -> prevSubListView.getItems().setAll(data)));
        }, null);
    }

    @FXML
    protected void goBackPrevSub() {
        prevSubListView.getSelectionModel().clearSelection();
        prevSubListView.getItems().clear();
        welcomePane.toFront();
    }

    @FXML
    protected void backHomePage() {
        welcomePane.toFront();
    }

    @FXML
    protected void showPersonalizedList() {
        List<String> list = PersonalizedListManager.getListsNames();
        if (list.isEmpty()) {
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.setTitle("Message");
            dialog.setHeaderText(null);
            dialog.setContentText("You don't have any list");
            dialog.initOwner(Launcher.get().mStage);
            dialog.showAndWait();
            return;
        }
        listNameListView.setItems(FXCollections.observableList(list));
        listNameListView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                listProblemListView.setItems(FXCollections.observableList(PersonalizedListManager.getProblemList(listNameListView.getSelectionModel().getSelectedItem())));
            }
        });
        listNameListView.getSelectionModel().selectFirst();

        listProblemListView.setItems(FXCollections.observableList(PersonalizedListManager.getProblemList(list.get(0))));
        personalizedListPane.toFront();
    }
}