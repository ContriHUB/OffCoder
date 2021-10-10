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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Coroutine<T> {

    public interface ProcessAsync<T> {
        T event();
    }

    public T runAsync(ProcessAsync<T> process, T def) {
        final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Future<T> future = null;
        try {
            future = threadPool.submit(process::event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert future != null;

        for (; ; ) {
            if (future.isDone()) {
                threadPool.shutdown();
                break;
            }
        }
        try {
            return future.get();
        } catch (Exception e) {
            return def;
        }
    }
}
