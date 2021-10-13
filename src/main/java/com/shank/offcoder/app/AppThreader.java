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

import javafx.application.Platform;

import java.util.Timer;
import java.util.TimerTask;

public class AppThreader {

    /**
     * Interface for simple callable block
     *
     * @param <T> Type of data to retrieve when called
     */
    public interface EventListener<T> {
        void onEvent(T data);
    }

    /**
     * A function to delay the execution
     *
     * @param process Code to execute of Void type
     * @param delay   Milli to delay
     */
    public static void delay(Runnable process, long delay) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(process);
            }
        }, delay);
    }
}
