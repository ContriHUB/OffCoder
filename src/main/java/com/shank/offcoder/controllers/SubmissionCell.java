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

public class SubmissionCell extends ListCell<Codeforces.PreviousSubmission> {

    private final Label dateLabel, problemName, lang, verdict, duration, mem;
    private final AnchorPane anchorPane;

    public SubmissionCell() {
        dateLabel = new Label("Label");
        dateLabel.setPrefWidth(150.0);
        dateLabel.setPrefHeight(32.0);

        problemName = new Label("Label");
        problemName.setPrefWidth(220.0);
        problemName.setPrefHeight(32.0);

        lang = new Label("Label");
        lang.setPrefWidth(71.0);
        lang.setPrefHeight(32.0);

        verdict = new Label("Label");
        verdict.setPrefWidth(173.0);
        verdict.setPrefHeight(32.0);

        duration = new Label("Label");
        duration.setPrefWidth(53.0);
        duration.setPrefHeight(32.0);

        mem = new Label("Label");
        mem.setPrefWidth(57.0);
        mem.setPrefHeight(32.0);

        anchorPane = new AnchorPane(dateLabel, problemName, lang, verdict, duration, mem);
        AnchorPane.setLeftAnchor(dateLabel, 20.0);
        AnchorPane.setLeftAnchor(problemName, 220.0);
        AnchorPane.setRightAnchor(lang, 324.0);
        AnchorPane.setRightAnchor(verdict, 141.0);
        AnchorPane.setRightAnchor(duration, 70.0);
        AnchorPane.setRightAnchor(mem, 15.0);
    }

    @Override
    protected void updateItem(Codeforces.PreviousSubmission item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        dateLabel.setText(item.date);
        problemName.setText(item.problemName);
        lang.setText(item.lang);
        verdict.setText(item.verdict);
        duration.setText(item.time);
        mem.setText(item.mem);
        setGraphic(anchorPane);

        setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (e.getClickCount() == 2) {
                    String[] codeName = item.problemName.split("-");
                    String code = codeName[0].trim();
                    String name = codeName[1].trim();
                    String url = String.format("/problemset/problem/%s/%c", code.substring(0, code.length() - 1), code.charAt(code.length() - 1));
                    boolean accepted = true;
                    ProblemParser.Problem p = new ProblemParser.Problem(code, name, url, "", accepted);
                    System.out.println("WEB: " + Codeforces.HOST + p.url);
                    ((Controller) Launcher.get().mFxmlLoader.getController()).loadWebPage(p);
                }
            }
        });
    }
}
