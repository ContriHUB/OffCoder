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
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for handling tasks for Codeforces
 */
public class Codeforces {

    private Codeforces() {}

    public static final String HOST = "https://codeforces.com";

    // --------- LOGIN/LOGOUT SPECIFIC CODE --------- //

    /**
     * Function to log in on codeforces
     *
     * @param handle   codeforces handle
     * @param password password for the handle
     */
    public static void login(String handle, String password, boolean remember, AppThreader.EventListener<String> listener) {
        AppData.get().writeData(AppData.AUTO_LOGIN_KEY, remember);
        AppData.get().writeData(AppData.HANDLE_KEY, handle);
        AppData.get().writeData(AppData.PASS_KEY, Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)));
        NetworkClient.get().login(body -> {
            FormElement formElement = (FormElement) body.select("form#enterForm").first();
            if (formElement != null) {
                System.out.println("Not logged in");

                AppData.get().writeData(AppData.HANDLE_KEY, AppData.NULL_STR);
                AppData.get().writeData(AppData.PASS_KEY, AppData.NULL_STR);
                AppData.get().writeData(AppData.AUTO_LOGIN_KEY, false);
                listener.onEvent("Error");
                return;
            }
            String mHandle;
            listener.onEvent((mHandle = findHandle(body.html())).isEmpty() ? "Error" : mHandle);
        });
    }

    /**
     * Function to log out from codeforces
     */
    public static void logout(AppThreader.EventListener<Boolean> listener) {
        NetworkClient.get().logout(data -> {
            Element regEl = data.select("a[href=\"/register\"]").first();
            listener.onEvent(regEl != null);
        });
    }

    private static String findHandle(String body) {
        int index = body.indexOf("handle = ");
        if (index == -1) return "";
        int end = body.indexOf("\"", index + 10);
        return body.substring(index + 10, end);
    }

    // --------- SUBMISSION SPECIFIC CODE --------- //

    /**
     * Class to store ID and extension based on language
     */
    public static class LangMeta {
        public String ext, ID;

        public LangMeta(String ext, String ID) {
            this.ext = ext;
            this.ID = ID;
        }
    }

    /**
     * Store the mapping of key being language and value {@link LangMeta}
     */
    private static final Map<String, LangMeta> mLangID = new HashMap<>();
    public static final String[] mLang = {"GNU GCC C11 5.1.0",
            "GNU G++11 5.1.0", "GNU G++14 6.4.0", "GNU G++17 7.3.0",
            "Java 11.0.5", "Kotlin 1.3.10",
            "Python 2.7.15", "Python 3.7.2"};

    static {
        mLangID.put("GNU GCC C11 5.1.0", new LangMeta(".c", "43"));
        mLangID.put("GNU G++11 5.1.0", new LangMeta(".cpp", "42"));
        mLangID.put("GNU G++14 6.4.0", new LangMeta(".cpp", "50"));
        mLangID.put("GNU G++17 7.3.0", new LangMeta(".cpp", "54"));
        mLangID.put("Java 11.0.5", new LangMeta(".java", "60"));
        mLangID.put("Kotlin 1.3.10", new LangMeta(".kt", "48"));
        mLangID.put("Python 2.7.15", new LangMeta(".py", "7"));
        mLangID.put("Python 3.7.2", new LangMeta(".py", "31"));
    }

    public static String getLangID(String lang) {return mLangID.getOrDefault(lang, new LangMeta("", "")).ID;}

    public static String getLangExt(String lang) {return mLangID.getOrDefault(lang, new LangMeta("", "")).ext;}
}
