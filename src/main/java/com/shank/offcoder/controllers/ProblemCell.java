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
import com.shank.offcoder.cf.ProblemParser;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;

/**
 * Class for custom item
 */
public class ProblemCell extends ListCell<ProblemParser.Problem> {

    private final Label problemCode, problemName, problemDiff;
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

        anchorPane = new AnchorPane(problemCode, problemName, problemDiff);
        anchorPane.setStyle("-fx-cursor: hand");
        AnchorPane.setLeftAnchor(problemCode, 20.0);
        AnchorPane.setTopAnchor(problemCode, 0.0);

        AnchorPane.setLeftAnchor(problemName, 90.0);
        AnchorPane.setTopAnchor(problemName, 0.0);

        AnchorPane.setRightAnchor(problemDiff, 14.0);
        AnchorPane.setTopAnchor(problemDiff, 0.0);
    }

    @Override
    protected void updateItem(ProblemParser.Problem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        problemCode.setText(item.code);
        problemName.setText(item.name);
        problemDiff.setText(item.rating);
        anchorPane.setStyle(item.accepted ? isSelected() ? "" : "-fx-background-color: #0BDA51" : "");
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
