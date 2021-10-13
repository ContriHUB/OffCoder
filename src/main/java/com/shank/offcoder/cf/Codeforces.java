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

import com.shank.offcoder.app.AppData;
import com.shank.offcoder.app.AppThreader;
import com.shank.offcoder.app.NetworkClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Class for handling tasks for Codeforces
 */
public class Codeforces {

    public static final String HOST = "https://codeforces.com";
    private static final String CHAR_DAT = "abcdefghijklmnopqrstuvwxyz0123456789";

    // UID needed for logout
    private static String LOG_OUT_UID = AppData.NULL_STR;

    public static String HANDLE = AppData.NULL_STR, PASS = AppData.NULL_STR;

    // --------- LOGIN/LOGOUT SPECIFIC CODE --------- //

    /**
     * Function to log in on codeforces
     *
     * @param handle   codeforces handle
     * @param password password for the handle
     */
    public static void login(String handle, String password, AppThreader.EventListener<String> listener) {
        String url = HOST + "/enter";
        NetworkClient.ReqGet(url, body -> {
            if (hasError(body)) {
                listener.onEvent("Codeforces down");
                return;
            }

            HashMap<String, String> params = new HashMap<>();
            params.put("csrf_token", getCsrf(body));
            params.put("action", "enter");
            params.put("ftaa", genFTAA());
            params.put("bfaa", "f1b3f18c715565b589b7823cda7448ce");
            params.put("handleOrEmail", handle);
            params.put("password", password);
            params.put("_tta", "176");
            params.put("remember", "on");

            String urlValues = params.keySet().stream()
                    .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&", url + "?", ""));

            NetworkClient.ReqPost(url, urlValues, reqPost -> {
                if (hasError(reqPost)) {
                    listener.onEvent("Codeforces down");
                    return;
                }

                String mHandle;
                LOG_OUT_UID = findLogOutUID(reqPost);
                if (!(mHandle = findHandle(reqPost)).isEmpty()) {
                    HANDLE = mHandle;
                    PASS = password;
                    listener.onEvent(mHandle);
                } else {
                    listener.onEvent("Login Failed");
                }
            });
        });
    }

    /**
     * Function to log out from codeforces
     */
    public static void logout(AppThreader.EventListener<Boolean> listener) {
        if (LOG_OUT_UID.equals(AppData.NULL_STR)) {
            listener.onEvent(false);
            return;
        }
        NetworkClient.ReqGet(HOST + "/" + LOG_OUT_UID + "/logout", data -> listener.onEvent(!data.isEmpty()));
    }

    private static String genFTAA() {
        StringBuilder ftaa = new StringBuilder();
        for (int i = 0; i < 18; i++) ftaa.append(CHAR_DAT.charAt(new Random().nextInt(CHAR_DAT.length())));
        return ftaa.toString();
    }

    private static String getCsrf(String body) {
        int index = body.indexOf("csrf='");
        if (index == -1) return "";
        int end = body.indexOf("'", index + 7);
        return body.substring(index + 6, end);
    }

    private static String findHandle(String body) {
        int index = body.indexOf("handle = ");
        if (index == -1) return "";
        int end = body.indexOf("\"", index + 10);
        return body.substring(index + 10, end);
    }

    private static String findLogOutUID(String body) {
        int index = body.indexOf("/logout");
        if (index == -1) return AppData.NULL_STR;
        int start = body.indexOf("/", index - 33);
        return body.substring(start + 1, index);
    }

    private static boolean hasError(String body) {
        try {
            Integer.parseInt(body.substring(0, 3));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
