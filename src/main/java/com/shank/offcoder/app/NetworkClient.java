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

package com.shank.offcoder.app;

import com.shank.offcoder.Launcher;
import com.shank.offcoder.cf.Codeforces;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.File;
import java.io.IOException;

/**
 * Class for handling networking.
 */
public class NetworkClient {

    public enum ARGS {LOGIN, LOGOUT, GET_PAGE}

    private Alert alert;

    private static volatile NetworkClient _instance = null;

    public static NetworkClient get() {
        if (_instance == null) _instance = new NetworkClient();
        return _instance;
    }

    private NetworkClient() {}

    /**
     * A function to check network connectivity
     *
     * @return false if connected
     */
    public static boolean isNetworkNotConnected() {
        try {
            Jsoup.connect(Codeforces.HOST).execute();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void login(AppThreader.EventListener<Document> listener) {
        Platform.runLater(() -> {
            alert = new Alert(Alert.AlertType.NONE, "Retrieving");
            alert.setTitle("Retrieving login page");
            alert.getButtonTypes().addAll(ButtonType.OK);
            alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
            alert.initOwner(Launcher.get().mStage);
            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
            alert.setOnShown(event -> new Thread(() -> {
                Document errDoc = Jsoup.parse("<html> <body> <p id=\"OffError\">Error</p> </body> </html>");
                try {
                    String ret = runClient(getCmd(ARGS.LOGIN));
                    Platform.runLater(() -> alert.close());
                    listener.onEvent(ret.isEmpty() ? errDoc : Jsoup.parse(ret));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> alert.close());
                    listener.onEvent(errDoc);
                }
            }).start());
            alert.show();
        });
    }

    public void logout(AppThreader.EventListener<Document> listener) {
        Platform.runLater(() -> {
            alert = new Alert(Alert.AlertType.NONE, "Retrieving");
            alert.setTitle("Retrieving logout page");
            alert.getButtonTypes().addAll(ButtonType.OK);
            alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
            alert.initOwner(Launcher.get().mStage);
            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
            alert.setOnShown(event -> new Thread(() -> {
                Document errDoc = Jsoup.parse("<html> <body> <p id=\"OffError\">Error</p> </body> </html>");
                try {
                    String ret = runClient(getCmd(ARGS.LOGOUT));
                    Platform.runLater(() -> alert.close());
                    listener.onEvent(ret.isEmpty() ? errDoc : Jsoup.parse(ret));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> alert.close());
                    listener.onEvent(errDoc);
                }
            }).start());
            alert.show();
        });
    }

    public void getPage(String URL, AppThreader.EventListener<Document> listener) {
        Platform.runLater(() -> {
            alert = new Alert(Alert.AlertType.NONE, "Retrieving");
            alert.setTitle("Retrieving Page");
            alert.getButtonTypes().addAll(ButtonType.OK);
            alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
            alert.initOwner(Launcher.get().mStage);
            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
            alert.setOnShown(event -> new Thread(() -> {
                Document errDoc = Jsoup.parse("<html> <body> <p id=\"OffError\">Error</p> </body> </html>");
                try {
                    String ret = runClient(getCmd(ARGS.GET_PAGE, URL));
                    Platform.runLater(() -> alert.close());
                    listener.onEvent(ret.isEmpty() ? errDoc : Jsoup.parse(ret));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> alert.close());
                    listener.onEvent(errDoc);
                }
            }).start());
            alert.show();
        });
    }

    /**
     * Runs the client/client.py with specific args from {@link #getCmd(ARGS, String...)}
     *
     * @return the retrieved page
     */
    private String runClient(String[] cmd) throws Exception {
        Process process = Runtime.getRuntime().exec(cmd);
        int exitCode = process.waitFor();
        if (exitCode != 0) return "";

        try {
            Document doc = Jsoup.parse(new File(AppData.get().getDataFolder(), "ref.html"), null);
            doc.outputSettings(doc.outputSettings().prettyPrint(false).escapeMode(Entities.EscapeMode.extended).charset("ASCII"));
            return doc.html().replaceAll("//codeforces.org", "https://codeforces.org");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * @return the cmd based on {@link ARGS}
     */
    private String[] getCmd(ARGS arg, String... args) {
        if (arg == ARGS.LOGIN) return new String[]{"python", "client/client.py", "login"};
        if (arg == ARGS.LOGOUT) return new String[]{"python", "client/client.py", "logout"};
        return new String[]{"python", "client/client.py", "get_page", args[0]};
    }
}
