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

package com.shank.offcoder.cli;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Class to handle presence of compiler
 * depending on OS.
 */
public class CompilerManager {

    public enum OSType {LINUX, WINDOWS}

    // Singleton instance
    private static volatile CompilerManager _instance = null;

    public static CompilerManager getInstance() {
        if (_instance == null) _instance = new CompilerManager();
        return _instance;
    }

    private interface CompilerCheck {
        void onResult();
    }

    private final OSType osType;
    private final List<String> mLang;

    private CompilerManager() {
        this.osType = detectOSType();
        this.mLang = new ArrayList<>();

        auditCompilers();
    }

    /**
     * Method to check all the compilers.
     * See {@link #auditCompilers(String[], CompilerCheck)}
     * for more details.
     */
    private void auditCompilers() {
        mLang.clear();
        auditCompilers(getCommandWithShell(new String[]{"javac --version"}),
                () -> mLang.add("Java 11.0.5"));

        auditCompilers(getCommandWithShell(new String[]{"gcc --version"}),
                () -> mLang.add("GNU GCC C11 5.1.0"));

        auditCompilers(getCommandWithShell(new String[]{"g++ --version"}), () -> {
            mLang.add("GNU G++14 6.4.0");
            mLang.add("GNU G++17 7.3.0");
        });

        auditCompilers(getCommandWithShell(new String[]{"python --version"}),
                () -> mLang.add("Python 2.7.15"));

        auditCompilers(getCommandWithShell(new String[]{"python3 --version"}),
                () -> mLang.add("Python 3.7.2"));
    }

    /**
     * Method to check if compiler of certain language exists.
     * If exists, call the {@link CompilerCheck#onResult()},
     * that will add the language selection in {@link #mLang}.
     *
     * @param command       Command for checking compiler
     * @param compilerCheck Callback onResult()
     */
    private void auditCompilers(String[] command, CompilerCheck compilerCheck) {
        CommandLine.runCommand(new CommandLine.ProcessListener() {
            @Override
            public void onCompleted(int exitCode, String output) {
                if (exitCode == 0) {
                    System.out.println("auditCompiler: " + Arrays.toString(command) + "; exitCode = " + exitCode);
                    compilerCheck.onResult();
                }
            }

            @Override
            public void onError(String err) {}
        }, command, false);
    }

    /**
     * Function that returns type of Operating System
     * {@link OSType#WINDOWS} or {@link OSType#LINUX}
     */
    private OSType detectOSType() {
        String system = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (system.contains("win")) return OSType.WINDOWS;
        return OSType.LINUX;
    }

    /**
     * @return shell depending on {@link OSType} for commands to get executed
     */
    private String[] getShell() {
        return osType == OSType.WINDOWS ? new String[]{"cmd.exe", "/c"} : new String[]{"/bin/bash", "-c"};
    }

    /**
     * @param command Command to executed
     * @return String[] command with valid shell
     */
    public String[] getCommandWithShell(String[] command) {
        String[] shell = getShell();

        String[] cmd = (String[]) Array.newInstance(command.getClass().getComponentType(), shell.length + command.length);
        System.arraycopy(shell, 0, cmd, 0, shell.length);
        System.arraycopy(command, 0, cmd, shell.length, command.length);
        return cmd;
    }

    public List<String> getLanguageList() {return mLang;}
}
