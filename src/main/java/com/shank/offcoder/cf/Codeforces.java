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
import javafx.application.Platform;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

/**
 * Class for handling tasks for Codeforces
 */
public class Codeforces {

    private Codeforces() {}

    public static final String HOST = "https://codeforces.com";
    private static final String CHAR_DAT = "abcdefghijklmnopqrstuvwxyz0123456789";

    // UID needed for logout
    private static String LOG_OUT_UID = AppData.NULL_STR;

    // --------- LOGIN/LOGOUT SPECIFIC CODE --------- //

    /**
     * Function to log in on codeforces
     *
     * @param handle   codeforces handle
     * @param password password for the handle
     */
    public static void login(String handle, String password, AppThreader.EventListener<String> listener) {
        NetworkClient.get().clearData();
        String url = HOST + "/enter";
        NetworkClient.get().ReqGet(url, body -> {
            FormElement formElement = (FormElement) body.select("form#enterForm").first();
            if (formElement == null) {
                System.out.println("Form NULL");
                listener.onEvent("Error");
                return;
            }
            String csrf = formElement.select("input[name=\"csrf_token\"]").val();
            NetworkClient.get().setParams(csrf, genFTAA(), handle, password);

            formElement.select("input#handleOrEmail").val(handle);
            formElement.select("input#password").val(password);
            try {
                Connection.Response response = formElement.submit().cookies(NetworkClient.get().getCookies()).data(NetworkClient.get().getParams())
                        .followRedirects(true).execute();
                NetworkClient.get().updateCookies(response.cookies());
                Document html = response.parse();

                LOG_OUT_UID = findLogOutUID(html.toString());
                String mHandle;
                listener.onEvent(response.statusCode() == 200 && !(mHandle = findHandle(html.toString())).isEmpty() ? mHandle : "Error");
            } catch (IOException e) {
                e.printStackTrace();
                listener.onEvent("Error");
            }
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
        NetworkClient.get().ReqGet(HOST + "/" + LOG_OUT_UID + "/logout", data -> {
            NetworkClient.get().clearData();
            listener.onEvent(!hasError(data));
        });
    }

    public static String genFTAA() {
        StringBuilder ftaa = new StringBuilder();
        for (int i = 0; i < 18; i++) ftaa.append(CHAR_DAT.charAt(new Random().nextInt(CHAR_DAT.length())));
        return ftaa.toString();
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

    private static boolean hasError(Document doc) {
        try {
            Element ele = doc.select("p#OffError").first();
            return ele != null && ele.val().equals("Error");
        } catch (Exception e) {
            return false;
        }
    }

    // --------- SUBMISSION SPECIFIC CODE --------- //

    public static class PreviousSubmission {
        public String date, sub_time, problemName, lang, verdict, time, mem;

        public PreviousSubmission(String date, String sub_time, String problemName, String lang, String verdict, String time, String mem) {
            this.date = date;
            this.sub_time = sub_time;
            this.problemName = problemName;
            this.lang = lang;
            this.verdict = verdict;
            this.time = time;
            this.mem = mem;
        }
    }

    public static void getPreviousSubmission(AppThreader.EventListener<List<PreviousSubmission>> listener) {
        NetworkClient.get().ReqGet("https://codeforces.com/problemset/status?my=on", data -> {
            List<PreviousSubmission> arr = new ArrayList<>();
            Elements submissions = data.select("table.status-frame-datatable").select("tr");
            for (int i = 1; i < submissions.size(); i++) {
                Elements subInfo = submissions.get(i).select("td");

                arr.add(new PreviousSubmission(subInfo.get(1).text(), subInfo.get(2).text(), subInfo.get(3).text(), subInfo.get(4).text(),
                        subInfo.get(5).text(), subInfo.get(6).text(), subInfo.get(7).text()));
            }
            listener.onEvent(arr);
        });
    }

    public static class Submission {
        public String lang, sourceCode;
        public ProblemParser.Problem pr;

        public Submission(String lang, String sourceCode, ProblemParser.Problem pr) {
            this.lang = lang;
            this.sourceCode = sourceCode;
            this.pr = pr;
        }
    }

    /**
     * Store the mapping of key being language and value exit
     */
    private static final Map<String, String> mLangID = new HashMap<>();
    public static final String[] mLang = {"GNU GCC C11 5.1.0",
            "GNU G++14 6.4.0", "GNU G++17 7.3.0",
            "Java 11.0.5", "Kotlin 1.3.10",
            "Python 2.7.15", "Python 3.7.2"};

    static {
        mLangID.put("GNU GCC C11 5.1.0", ".c");
        mLangID.put("GNU G++14 6.4.0", ".cpp");
        mLangID.put("GNU G++17 7.3.0", ".cpp");
        mLangID.put("Java 11.0.5", ".java");
        mLangID.put("Kotlin 1.3.10", ".kt");
        mLangID.put("Python 2.7.15", ".py");
        mLangID.put("Python 3.7.2", ".py");
    }

    public static String getLangExt(String lang) {return mLangID.getOrDefault(lang, "");}

    public static void submitCode(Submission submission, AppThreader.EventListener<SubmissionQueue.PostResult> listener) {
        NetworkClient.get().ReqGet(HOST + submission.pr.url + "?csrf_token=" + NetworkClient.get().getParams().get("csrf_token"), data -> {
            FormElement formElement = (FormElement) data.select("form.submitForm").first();
            if (formElement == null) {
                System.out.println("Submission FORM NULL");
                Platform.runLater(() -> listener.onEvent(new SubmissionQueue.PostResult(false, submission.pr.code)));
                return;
            }

            Elements option = formElement.select("select > option");
            for (Element el : option) {
                if (el.attr("selected").equals("selected")) {
                    if (!el.text().trim().equals(submission.lang)) el.removeAttr("selected");
                } else {
                    if (el.text().trim().equals(submission.lang)) el.attr("selected", "selected");
                }
            }

            formElement.select("input[name=\"source\"]").val(submission.sourceCode);

            try {
                Connection.Response response = formElement.submit().cookies(NetworkClient.get().getCookies()).data(NetworkClient.get().getParams())
                        .followRedirects(true).execute();
                NetworkClient.get().updateCookies(response.cookies());
                Platform.runLater(() -> listener.onEvent(new SubmissionQueue.PostResult(true, submission.pr.code)));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> listener.onEvent(new SubmissionQueue.PostResult(false, submission.pr.code)));
            }
        });
    }
}
