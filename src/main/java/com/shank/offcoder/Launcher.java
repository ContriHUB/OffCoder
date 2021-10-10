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

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 400);
        stage.setTitle("OffCoder");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(400);
        stage.show();

        if (AppData.get().<Boolean>readData(AppData.AUTO_LOGIN_KEY, false)) {
            ((Controller) fxmlLoader.getController()).attemptLogin(AppData.get().readData(AppData.HANDLE_KEY, AppData.NULL_STR),
                    new String(Base64.getDecoder().decode(AppData.get().readData(AppData.PASS_KEY, AppData.NULL_STR))));
        }
    }

    public static void main(String[] args) {
        NetworkClient.InitClient();
        launch();
    }
}