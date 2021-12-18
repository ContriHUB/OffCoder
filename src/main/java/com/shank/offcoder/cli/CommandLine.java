package com.shank.offcoder.cli;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class CommandLine {

    public static final int TIME_OUT_EXIT = 47732;

    /**
     * Interface to listen program execution
     */
    public interface ProcessListener {
        void onCompleted(int exitCode, String output);

        void onError(String err);
    }

    /**
     * Method to execute command
     * <p>
     * Executes a shell command at runtime which creates a {@link Process}.
     * This process is then handled accordingly with below parameters
     *
     * @param listener         Callback after process execution
     * @param command          Command to be executed
     * @param isTimeConstraint Whether to wait within time constraint (1 sec)
     */
    public static void runCommand(ProcessListener listener, String[] command, boolean isTimeConstraint) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            InputStream is = process.getInputStream();
            InputStream errStream = process.getErrorStream();

            if (isTimeConstraint) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        boolean alive = process.isAlive();
                        process.children().forEach(ProcessHandle::destroyForcibly);
                        listener.onCompleted(alive ? TIME_OUT_EXIT : 0, alive ? "" : getStream(is));
                    }
                }, 1000);
            } else {
                int exitCode = process.waitFor();
                if (process.exitValue() != 0) {
                    listener.onError(getStream(errStream));
                }
                listener.onCompleted(exitCode, getStream(is));
            }
        } catch (Exception e) {
            listener.onCompleted(-1, "");
        }
    }

    /**
     * @return String data from {@link InputStream}
     */
    private static String getStream(InputStream is) {
        try {
            StringBuilder sb = new StringBuilder();
            int val;
            while ((val = is.read()) != -1) sb.append((char) val);
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
