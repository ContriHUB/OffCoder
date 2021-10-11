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
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Base64;

public class Launcher extends Application {

    Stage mStage = null;

    private static Launcher _instance = null;

    public Launcher() {_instance = this;}

    public static Launcher get() {return _instance;}

    @Override
    public void start(Stage stage) throws IOException {
        mStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 400);
        mStage.setTitle("OffCoder");
        mStage.setScene(scene);
        mStage.setMinWidth(800);
        mStage.setMinHeight(400);
        limitWindowSize();
        mStage.show();

        if (AppData.get().<Boolean>getData(AppData.AUTO_LOGIN_KEY, false)) {
            ((Controller) fxmlLoader.getController()).attemptLogin(AppData.get().getData(AppData.HANDLE_KEY, AppData.NULL_STR),
                    new String(Base64.getDecoder().decode(AppData.get().getData(AppData.PASS_KEY, AppData.NULL_STR))));
        }
    }

    public void limitWindowSize() {
        mStage.setMaxWidth(800);
        mStage.setMaxHeight(400);
    }

    public void freeWindowSize() {
        mStage.setMaxWidth(Double.MAX_VALUE);
        mStage.setMaxHeight(Double.MAX_VALUE);
    }

    public static void main(String[] args) {
        NetworkClient.InitClient();
        launch();
    }
}