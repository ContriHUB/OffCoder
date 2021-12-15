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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

/**
 * Class for handling app data
 */
public class AppData {

    // Singleton instance
    private static volatile AppData _instance = null;

    private static final String APP_DATA_FILE = "cf_coder.dat";
    public static final String NULL_STR = "&^";

    // Keys used in JSON

    // Key for handle
    public static final String HANDLE_KEY = "handle";

    // Key for password (stored in encoded Base64)
    public static final String PASS_KEY = "password";

    // Key for whether to automatically login on launch
    public static final String AUTO_LOGIN_KEY = "auto_login";

    // Key for saving downloaded problems
    public static final String DOWNLOADED_QUES = "downloaded_ques";

    public static final String P_RATING_KEY = "rating";
    public static final String P_CODE_KEY = "code";
    public static final String P_URL_KEY = "url";
    public static final String P_HTML_KEY = "html";
    public static final String P_NAME_KEY = "name";
    public static final String P_ACCEPTED_KEY = "accepted";

    // Main json to hold all the data
    private static JSONObject mData = null;

    private AppData() {
        // Initialize folder and files
        File dat = new File(getDataFolder(), APP_DATA_FILE);
        if (dat.exists()) {
            readFile(dat);
        } else {
            mData = new JSONObject();
            mData.put(HANDLE_KEY, NULL_STR);
            mData.put(PASS_KEY, NULL_STR);
            mData.put(AUTO_LOGIN_KEY, false);

            writeFile(dat);
        }
    }

    // Get singleton instance
    public static AppData get() {
        if (_instance == null) _instance = new AppData();
        return _instance;
    }

    /**
     * Function to write/save data
     *
     * @param key   The key to store value
     * @param <T>   Type of value to store
     * @param value value to store
     */
    public <T> void writeData(String key, T value) {
        mData.put(key, value);
        writeFile(new File(getDataFolder(), APP_DATA_FILE));
    }

    /**
     * Function to retrieve the data
     *
     * @param key To get data from that key
     * @param <T> Type of data to return
     * @param def Return default in case key is absent
     */
    public <T> T getData(String key, T def) {
        try {
            Object obj = mData.get(key);
            return (T) obj;
        } catch (Exception e) {
            return def;
        }
    }

    public void clearData() {
        writeData(AUTO_LOGIN_KEY, false);
        writeData(HANDLE_KEY, NULL_STR);
        writeData(PASS_KEY, NULL_STR);
        writeData(DOWNLOADED_QUES, new JSONArray());
    }

    // -----

    public File getDataFolder() {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private void readFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder _rDat = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) _rDat.append(line).append("\n");

            mData = new JSONObject(_rDat.toString());
        } catch (Exception e) {
            mData = null;
        }
    }

    private void writeFile(File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(mData.toString(4).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
