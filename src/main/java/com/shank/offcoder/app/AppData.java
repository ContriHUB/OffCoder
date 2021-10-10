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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

public class AppData {

    private static volatile AppData _instance = null;

    private static final String APP_DATA_FILE = "cf_coder.dat";
    public static final String NULL_STR = "&^";

    public static final String HANDLE_KEY = "handle";
    public static final String PASS_KEY = "password";
    public static final String AUTO_LOGIN_KEY = "auto_login";

    private static JSONObject mData = null;

    private AppData() {
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

    public static AppData get() {
        if (_instance == null) {
            _instance = new AppData();
        }
        return _instance;
    }

    public <T> void writeData(String key, T value) {
        mData.put(key, value);
        writeFile(new File(getDataFolder(), APP_DATA_FILE));
    }

    public <T> T readData(String key, T def) {
        try {
            Object obj = mData.get(key);
            return (T) obj;
        } catch (Exception e) {
            return def;
        }
    }

    // -----

    private File getDataFolder() {
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
