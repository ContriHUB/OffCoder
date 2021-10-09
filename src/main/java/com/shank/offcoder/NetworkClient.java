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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class NetworkClient {

    private static volatile NativeNetworkClient networkClient = null;

    private static synchronized NativeNetworkClient get() {
        if (networkClient == null) {
            System.setProperty("java.library.path", "D:/IdeaProjects/OffCoder/libs/;" + System.getProperty("java.library.path"));
            networkClient = Native.loadLibrary("network_client", NativeNetworkClient.class);
        }
        return networkClient;
    }

    private static NativeNetworkClient.GoString.ByValue getGoString(String str) {
        NativeNetworkClient.GoString.ByValue goStr = new NativeNetworkClient.GoString.ByValue();
        goStr.p = str;
        goStr.n = goStr.p.length();
        return goStr;
    }

    public static void InitClient() {
        get().InitClient();
    }

    public static String ReqGet(String URL) {
        return get().ReqGet(getGoString(URL));
    }

    public static String ReqPost(String URL, String urlParams) {
        return get().ReqPost(getGoString(URL), getGoString(urlParams));
    }

    public interface NativeNetworkClient extends Library {

        public class GoString extends Structure {
            public static class ByValue extends GoString implements Structure.ByValue {
            }

            public String p;
            public long n;

            protected List getFieldOrder() {
                return Arrays.asList("p", "n");
            }
        }

        public void InitClient();

        public String ReqGet(GoString.ByValue URL);

        public String ReqPost(GoString.ByValue URL, GoString.ByValue urlParams);
    }
}
