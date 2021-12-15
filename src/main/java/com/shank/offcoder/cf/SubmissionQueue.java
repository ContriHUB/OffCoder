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

package com.shank.offcoder.cf;

import com.shank.offcoder.Launcher;
import com.shank.offcoder.app.AppData;
import com.shank.offcoder.app.AppThreader;
import com.shank.offcoder.app.NetworkClient;
import com.shank.offcoder.controllers.Controller;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

public class SubmissionQueue {

    private boolean mWorking = false;
    private int numQueued = 0;

    // Singleton instanced to persist `mWorking`
    private static volatile SubmissionQueue _instance = null;

    public static SubmissionQueue get() {
        if (_instance == null) _instance = new SubmissionQueue();
        return _instance;
    }

    private SubmissionQueue() {}

    /**
     * Class for storing result from submitting code
     */
    public static class PostResult {
        public boolean submitted;
        public String code;

        public PostResult(boolean submitted, String code) {
            this.submitted = submitted;
            this.code = code;
        }
    }

    /**
     * Function to queue the submission.
     * <p>
     * Creates a timer task and runs at interval of 2 sec.
     * {@link #mWorking} is used to execute only one task at any time.
     */
    public void queue(Codeforces.Submission submission, AppThreader.EventListener<PostResult> listener) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (NetworkClient.isNetworkNotConnected()) return;
                if (mWorking) return;
                mWorking = true;

                System.out.println("Executing: " + submission.pr.code);
                Codeforces.login(AppData.get().getData(AppData.HANDLE_KEY, AppData.NULL_STR),
                        new String(Base64.getDecoder().decode(AppData.get().getData(AppData.PASS_KEY, AppData.NULL_STR))),
                        unused -> {
                            Codeforces.submitCode(submission, listener);
                            if (numQueued > 0) --numQueued;
                            Platform.runLater(() -> ((Controller) Launcher.get().mFxmlLoader.getController()).updateQueueLab("Queued: " + numQueued));
                        });
                mWorking = false;
                timer.cancel();
            }
        }, 0, 2000);
        System.out.println("Queued: " + submission.pr.code);
        ++numQueued;
        ((Controller) Launcher.get().mFxmlLoader.getController()).updateQueueLab("Queued: " + numQueued);

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Submission Queued");
        dialog.setHeaderText(null);
        dialog.setContentText("Submission will be executed when connect to network.");
        dialog.initOwner(Launcher.get().mStage);
        dialog.showAndWait();
    }
}
