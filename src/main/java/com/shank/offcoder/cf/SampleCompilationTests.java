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
import com.shank.offcoder.cli.CommandLine;
import com.shank.offcoder.cli.CompilerManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for compiling and validating sample test cases
 */
public class SampleCompilationTests {

    private final CompilerManager mCompilerManager = CompilerManager.getInstance();
    private final List<String> mFailed = new ArrayList<>();
    private String mSourceCode = AppData.NULL_STR, mExt = AppData.NULL_STR, mDoc = AppData.NULL_STR;
    private Alert alert;

    public void setDoc(String html) {
        mDoc = html;
    }

    public String getExt() {
        return mExt;
    }

    public String getSourceCode() {
        return mSourceCode;
    }

    public boolean setSourceFile(File sourceFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) sb.append(line).append("\n");

            mSourceCode = sb.toString();
            mExt = getExt(sourceFile);
            System.out.println("Source code set: " + mSourceCode);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Starts compiling and testing
     */
    public void compile(String lang) {
        if (!readyToCompile()) return;
        File compileCode = writeTempCode();
        if (compileCode == null) return;

        alert = new Alert(Alert.AlertType.NONE, "Compiling");
        alert.setTitle("Compiling");
        alert.getButtonTypes().addAll(ButtonType.OK);
        alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
        alert.initOwner(Launcher.get().mStage);
        alert.setOnShown(event -> {
            if (lang.contains("GNU")) {
                String[] cmd = mCompilerManager.getCommandWithShell(getCommand(compileCode, lang));
                System.out.println(Arrays.toString(cmd));

                CommandLine.runCommand(new CommandLine.ProcessListener() {
                    @Override
                    public void onCompleted(int exitCode, String output) {
                        if (exitCode == 0) {
                            alert.setTitle("Running tests");
                            alert.setContentText("Running tests");
                            System.out.println("Cmd: completed output : " + output);
                            mFailed.clear();
                            runTests(getTestCases(), new String[]{"data/temp.exe", "<", "data/input.txt"}, 0);
                        }
                    }

                    @Override
                    public void onError(String err) {
                        Platform.runLater(() -> {
                            if (alert != null) {
                                alert.close();
                                alert = null;
                            }
                            Alert failedDialog;
                            failedDialog = new Alert(Alert.AlertType.ERROR);
                            failedDialog.setTitle("Compilation Error");
                            failedDialog.setContentText(err);
                            failedDialog.initOwner(Launcher.get().mStage);
                            failedDialog.showAndWait();
                        });
                        System.out.println("Cmd: err : " + err);
                    }
                }, cmd, false);
            } else {
                alert.setTitle("Running tests");
                alert.setContentText("Running tests");

                String exe;
                if (lang.contains("Python 2")) {
                    exe = "python2";
                } else if (lang.contains("Python 3")) {
                    exe = "python3";
                } else {
                    exe = "java";
                }
                runTests(getTestCases(), new String[]{exe, "data/temp" + mExt, "<", "data/input.txt"}, 0);
            }
        });
        alert.show();
    }

    public boolean readyToCompile() {
        return !mSourceCode.equals(AppData.NULL_STR) && !mDoc.equals(AppData.NULL_STR);
    }

    /**
     * A recursive function to run tests
     */
    private void runTests(List<SampleTests> list, String[] exe, int idx) {
        if (idx == list.size()) {
            Platform.runLater(() -> {
                if (alert != null) {
                    alert.close();
                    alert = null;
                }
                Alert resultDialog;
                if (mFailed.isEmpty()) {
                    resultDialog = new Alert(Alert.AlertType.INFORMATION);
                    resultDialog.setTitle("Test case(s) passed");
                    resultDialog.setHeaderText("All cased passed.");
                } else {
                    resultDialog = new Alert(Alert.AlertType.ERROR);
                    resultDialog.setTitle("Test case(s) failed");

                    StringBuilder sb = new StringBuilder();
                    for (String s : mFailed) sb.append(s).append("\n");
                    resultDialog.setContentText(sb.toString());
                }
                resultDialog.initOwner(Launcher.get().mStage);
                resultDialog.showAndWait();
                mFailed.clear();
            });
            return;
        }
        writeTest(list.get(idx).input);
        CommandLine.runCommand(new CommandLine.ProcessListener() {
            @Override
            public void onCompleted(int exitCode, String output) {
                if (exitCode == 0 || exitCode == CommandLine.TIME_OUT_EXIT) {
                    output = output.replaceAll("\r", "");
                    String expOutput = list.get(idx).output;

                    if (exitCode == CommandLine.TIME_OUT_EXIT) {
                        mFailed.add("Test " + (idx + 1) + " failed:\nTIME LIMIT EXCEEDED");
                    } else if (outputMatches(output, expOutput)) {
                        System.out.println("Passed: Expected: " + expOutput + "; Got: " + output);
                    } else {
                        mFailed.add("Test " + (idx + 1) + " failed:\nExpected: " + expOutput + " - Got: " + output);
                        System.out.println("Failed: Expected: " + expOutput + "; Got: " + output);
                    }
                    runTests(list, exe, idx + 1);
                }
            }

            @Override
            public void onError(String err) {
                System.out.println("Exec: err : " + err);
            }
        }, mCompilerManager.getCommandWithShell(exe), true);
    }

    /**
     * Function that checks the program output and expected output
     * line by line.
     *
     * @return if outputs matches
     */
    private boolean outputMatches(String output, String expectedOutput) {
        String[] outputLines = output.split("\n"), expectedLines = expectedOutput.split("\n");
        if (outputLines.length != expectedLines.length) return false;

        for (int i = 0; i < outputLines.length; i++) {
            if (!outputLines[i].trim().equals(expectedLines[i].trim())) return false;
        }
        return true;
    }

    /**
     * @return compilation command based on language
     */
    private String[] getCommand(File compileFile, String lang) {
        if (mExt.equals(".cpp")) {
            if (lang.contains("G++")) {
                int index = lang.indexOf("G++");
                return new String[]{"g++", compileFile.getAbsolutePath(), "-std=c++" + lang.substring(index + 3, index + 5), "-o", "data/temp.exe"};
            }
            return new String[]{"g++", compileFile.getAbsolutePath(), "-o", "data/temp.exe"};
        } else {
            return new String[]{"gcc", compileFile.getAbsolutePath(), "-o", "data/temp.exe"};
        }
    }

    /**
     * Write sample test input to file to pass it to running program
     */
    private void writeTest(String input) {
        try (FileOutputStream fos = new FileOutputStream(new File(AppData.get().getDataFolder(), "input.txt"))) {
            fos.write(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to copy the {@link #mSourceCode} to local storage for compilation
     */
    private File writeTempCode() {
        File tempFile = new File(AppData.get().getDataFolder(), "temp" + mExt);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(mSourceCode.getBytes(StandardCharsets.UTF_8));
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return extension of file
     */
    private String getExt(File file) {
        String name = file.getName();
        int index = name.indexOf(".", name.length() - 5);
        return name.substring(index);
    }

    /**
     * Parses {@link #mDoc}
     *
     * @return list of sample test cases
     */
    private List<SampleTests> getTestCases() {
        List<SampleTests> arr = new ArrayList<>();
        Document doc = Jsoup.parse(mDoc);

        Elements inputs = doc.select("div.sample-test").select("div.input"),
                outputs = doc.select("div.sample-test").select("div.output");
        for (Element test : inputs) {
            SampleTests sampleTests = new SampleTests();
            StringBuilder inp = new StringBuilder();

            Element inputEle = test.select("pre").first();
            if (inputEle == null) continue;
            for (Node n : inputEle.childNodes()) {
                if (n.toString().equals("<br>")) continue;
                inp.append(n).append("\n");
            }
            sampleTests.input = inp.toString();
            arr.add(sampleTests);
        }

        int idx = 0;
        for (Element test : outputs) {
            StringBuilder out = new StringBuilder();

            Element outputEle = test.select("pre").first();
            if (outputEle == null) continue;
            for (Node n : outputEle.childNodes()) {
                if (n.toString().equals("<br>")) continue;
                out.append(n).append("\n");
            }
            arr.get(idx).output = out.toString().replaceAll(new String(new char[]{10}), "\n");
            ++idx;
        }
        return arr;
    }

    /**
     * Class to store each sample test
     */
    public static class SampleTests {
        public String input, output;

        public SampleTests() {
        }

        @Override
        public String toString() {
            return "SampleTests{" +
                    "input='" + input + '\'' +
                    ", output='" + output + '\'' +
                    '}';
        }
    }
}
