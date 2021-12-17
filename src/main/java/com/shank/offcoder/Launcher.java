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
import com.shank.offcoder.controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Base64;

public class Launcher extends Application {

    public Stage mStage = null;
    public FXMLLoader mFxmlLoader = null;

    // Singleton instance of application
    private static Launcher _instance = null;

    public Launcher() {_instance = this;}

    public static Launcher get() {return _instance;}

    /**
     * Method that starts main window
     */
    @Override
    public void start(Stage stage) throws IOException {
        mStage = stage;
        centerWindow(800, 400);

        // Load UI
        mFxmlLoader = new FXMLLoader(Launcher.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(mFxmlLoader.load(), 800, 400);
        mStage.setTitle("OffCoder");
        mStage.setScene(scene);
        mStage.setMinWidth(800);
        mStage.setMinHeight(400);
        limitWindowSize();
        mStage.show();

        // Check whether auto login was enabled
        // Attempt login if enabled
        if (AppData.get().<Boolean>getData(AppData.AUTO_LOGIN_KEY, false)) {
            ((Controller) mFxmlLoader.getController()).attemptLogin(AppData.get().getData(AppData.HANDLE_KEY, AppData.NULL_STR),
                    new String(Base64.getDecoder().decode(AppData.get().getData(AppData.PASS_KEY, AppData.NULL_STR))), true);
        }
    }

    /**
     * Method to limit window size
     * - used in login page
     */
    public void limitWindowSize() {
        mStage.setHeight(400);
        mStage.setWidth(800);
        mStage.setMaxWidth(800);
        mStage.setMaxHeight(400);
        centerWindow(800, 400);
    }

    /**
     * Method to free the size of window
     * - called after login page
     */
    public void freeWindowSize() {
        mStage.setHeight(800);
        mStage.setWidth(1600);
        mStage.setMaxWidth(Double.MAX_VALUE);
        mStage.setMaxHeight(Double.MAX_VALUE);
        centerWindow(1600, 800);
    }

    public void centerWindow(int width, int height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        mStage.setX((screenBounds.getWidth() - width) / 2);
        mStage.setY((screenBounds.getHeight() - height) / 2);
    }

    // MAIN LAUNCHER
    public static void main(String[] args) {launch();}
}