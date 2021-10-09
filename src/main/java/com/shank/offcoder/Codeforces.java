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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

public class Codeforces {

    private static final String HOST = "https://codeforces.com";
    private static final String CHAR_DAT = "abcdefghijklmnopqrstuvwxyz0123456789";

    public static void login(String handle, String password) {
        // API Key: 59da14a9f3844d44baa9d77a1e241b4459afd133
        String url = HOST + "/enter";
        String body = NetworkClient.ReqGet(url);

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
        try {
            String reqPost = NetworkClient.ReqPost(url, urlValues), mHandle;
            if (!(mHandle = findHandle(reqPost)).isEmpty()) {
                System.out.println("Welcome " + mHandle + "!");
            } else {
                System.out.println("Login Failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String genFTAA() {
        StringBuilder ftaa = new StringBuilder();
        for (int i = 0; i < 18; i++) ftaa.append(CHAR_DAT.charAt(new Random().nextInt(CHAR_DAT.length())));
        return ftaa.toString();
    }

    public static String getCsrf(String body) {
        int index = body.indexOf("csrf='");
        int end = body.indexOf("'", index + 7);
        return body.substring(index + 6, end);
    }

    public static String findHandle(String body) {
        int index = body.indexOf("handle = ");
        if (index == -1) return "";
        int end = body.indexOf("\"", index + 10);
        return body.substring(index + 10, end);
    }
}
