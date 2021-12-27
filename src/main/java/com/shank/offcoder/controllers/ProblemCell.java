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
import com.shank.offcoder.cf.Codeforces;
import com.shank.offcoder.cf.DownloadManager;
import com.shank.offcoder.cf.ProblemParser;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * Class for custom item
 */
public class ProblemCell extends ListCell<ProblemParser.Problem> {

    private final Label problemCode, problemName, problemDiff;
    private final SVGPath downloadImage, acceptedImage;
    private final AnchorPane anchorPane;

    public ProblemCell() {
        problemCode = new Label("Label");
        problemCode.setPrefWidth(53.0);
        problemCode.setPrefHeight(32.0);

        problemName = new Label("Label");
        problemName.setPrefWidth(295.0);
        problemName.setPrefHeight(32.0);

        problemDiff = new Label("Label");
        problemDiff.setPrefWidth(57.0);
        problemDiff.setPrefHeight(32.0);

        downloadImage = new SVGPath();
        acceptedImage = new SVGPath();

        anchorPane = new AnchorPane(problemCode, problemName, acceptedImage, problemDiff, downloadImage);
        anchorPane.setStyle("-fx-cursor: hand");
        AnchorPane.setLeftAnchor(problemCode, 20.0);
        AnchorPane.setTopAnchor(problemCode, 0.0);

        AnchorPane.setLeftAnchor(problemName, 90.0);
        AnchorPane.setTopAnchor(problemName, 0.0);

        AnchorPane.setRightAnchor(problemDiff, 50.0);
        AnchorPane.setTopAnchor(problemDiff, 0.0);

        AnchorPane.setRightAnchor(acceptedImage, 155.0);
        AnchorPane.setTopAnchor(acceptedImage, 8.0);

        AnchorPane.setRightAnchor(downloadImage, 14.0);
        AnchorPane.setTopAnchor(downloadImage, 6.0);
    }

    @Override
    protected void updateItem(ProblemParser.Problem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        boolean questionDownloaded = DownloadManager.isQuestionDownloaded(item.code);
        downloadImage.setContent(questionDownloaded ? "M12,2C6.49,2,2,6.49,2,12s4.49,10,10,10s10-4.49,10-10S17.51,2,12,2z M12,20c-4.41,0-8-3.59-8-8s3.59-8,8-8s8,3.59,8,8 S16.41,20,12,20z M14.59,8.59L16,10l-4,4l-4-4l1.41-1.41L11,10.17V6h2v4.17L14.59,8.59z M17,17H7v-2h10V17z" : "");
        downloadImage.setFill(Color.web("#1a73e8"));

        acceptedImage.setContent(item.accepted ? "M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z" : "");
        acceptedImage.setFill(Color.web("#0BDA51"));

        problemCode.setText(item.code);
        problemCode.setTextFill(Color.web("#000000"));

        problemName.setText(item.name);
        problemName.setTextFill(Color.web("#000000"));

        problemDiff.setText(item.rating);
        problemDiff.setTextFill(Color.web("#000000"));

        anchorPane.setStyle("-fx-border-radius: 8px;-fx-background-radius: 8px;" + (isSelected() ? "-fx-background-color: #FFFFFF" : ""));
        setGraphic(anchorPane);

        setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (e.getClickCount() == 2) {
                    System.out.println("WEB: " + Codeforces.HOST + item.url);
                    ((Controller) Launcher.get().mFxmlLoader.getController()).loadWebPage(item);
                }
            }
        });
    }
}
