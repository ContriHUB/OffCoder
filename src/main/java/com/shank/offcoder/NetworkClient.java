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

package com.shank.offcoder;

import com.shank.offcoder.app.Coroutine;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Class for handling networking
 * from native client.
 */
public class NetworkClient {

    // Singleton instance of native network client
    private static volatile NativeNetworkClient networkClient = null;

    // Creating instance by loading native dll
    private static synchronized NativeNetworkClient get() {
        if (networkClient == null) {
            System.setProperty("java.library.path", "D:/IdeaProjects/OffCoder/libs/;" + System.getProperty("java.library.path"));
            networkClient = Native.loadLibrary("network_client", NativeNetworkClient.class);
        }
        return networkClient;
    }

    /**
     * A function to convert {@link String} to {@link NativeNetworkClient.GoString}
     * needed by {@link NativeNetworkClient}.
     */
    private static NativeNetworkClient.GoString.ByValue getGoString(String str) {
        NativeNetworkClient.GoString.ByValue goStr = new NativeNetworkClient.GoString.ByValue();
        goStr.p = str;
        goStr.n = goStr.p.length();
        return goStr;
    }

    /**
     * A function to check network connectivity
     *
     * @return false if connected
     */
    public static boolean isNetworkNotConnected() {
        return new Coroutine<Boolean>().runAsync(() -> {
            try {
                new URL("https://www.google.com").openConnection().connect();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }, false);
    }

    // ---------- Functions that extend to native lib ---------- //

    /**
     * Initialize native client
     */
    public static void InitClient() {
        get().InitClient();
    }

    /**
     * Execute GET on given
     *
     * @param URL URL for GET
     */
    public static String ReqGet(String URL) {
        return new Coroutine<String>().runAsync(() -> get().ReqGet(getGoString(URL)), "");
    }

    /**
     * Execute POST on given
     *
     * @param URL       URL for POST
     * @param urlParams Params for the URL
     */
    public static String ReqPost(String URL, String urlParams) {
        return new Coroutine<String>().runAsync(() -> get().ReqPost(getGoString(URL), getGoString(urlParams)), "");
    }

    /**
     * Interface that binds to native client (lib/dll)
     */
    public interface NativeNetworkClient extends Library {

        class GoString extends Structure {
            public static class ByValue extends GoString implements Structure.ByValue {
            }

            public String p;
            public long n;

            protected List<String> getFieldOrder() {
                return Arrays.asList("p", "n");
            }
        }

        void InitClient();

        String ReqGet(GoString.ByValue URL);

        String ReqPost(GoString.ByValue URL, GoString.ByValue urlParams);
    }
}
