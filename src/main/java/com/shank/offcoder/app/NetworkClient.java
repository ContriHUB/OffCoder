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

import com.shank.offcoder.cf.Codeforces;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for handling networking.
 */
public class NetworkClient {

    // Cookies for persisting session
    private final Map<String, String> mCookies = new HashMap<>(), mParams = new HashMap<>();

    private static volatile NetworkClient _instance = null;

    public static NetworkClient get() {
        if (_instance == null) _instance = new NetworkClient();
        return _instance;
    }

    private NetworkClient() {}

    public void setParams(String csrf, String ftaa, String handle, String password) {
        mParams.put("csrf_token", csrf);
        mParams.put("action", "enter");
        mParams.put("ftaa", ftaa);
        mParams.put("bfaa", "f1b3f18c715565b589b7823cda7448ce");
        mParams.put("handleOrEmail", handle);
        mParams.put("password", password);
        mParams.put("_tta", "176");
        mParams.put("remember", "on");
    }

    public Map<String, String> getCookies() {return mCookies;}

    public void updateCookies(Map<String, String> _nCookies) {mCookies.putAll(_nCookies);}

    public Map<String, String> getParams() {return mParams;}

    public void clearData() {
        mCookies.clear();
        mParams.clear();
    }

    /**
     * A function to check network connectivity
     *
     * @return false if connected
     */
    public static boolean isNetworkNotConnected() {
        try {
            Jsoup.connect(Codeforces.HOST).execute();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Execute GET on given
     *
     * @param URL URL for GET
     */
    public void ReqGet(String URL, AppThreader.EventListener<Document> listener) {
        new Thread(() -> {
            Document errDoc = Jsoup.parse("<html> <body> <p id=\"OffError\">Error</p> </body> </html>");
            try {
                Connection connection = Jsoup.connect(URL).method(Connection.Method.GET).followRedirects(true);
                Connection.Response response = connection.cookies(mCookies).data(mParams.isEmpty() ? new HashMap<>() : mParams).execute();
                mCookies.putAll(response.cookies());
                listener.onEvent(response.statusCode() == 200 ? response.parse() : errDoc);
            } catch (Exception e) {
                e.printStackTrace();
                listener.onEvent(errDoc);
            }
        }).start();
    }
}