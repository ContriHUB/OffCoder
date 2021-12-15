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
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProblemParser {

    private int minDifficulty = 0, maxDifficulty = 800, page = 1;

    /**
     * Data class for problems
     */
    public static class Problem {
        public String code, name, url, rating;
        public boolean accepted = false;

        public Problem() {}

        public Problem(String code, String name, String url, String rating, boolean accepted) {
            this.code = code;
            this.name = name;
            this.url = url;
            this.rating = rating;
            this.accepted = accepted;
        }

        @Override
        public String toString() {
            return "Problem{" +
                    "code='" + code + '\'' +
                    ", name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", accepted=" + accepted +
                    ", rating=" + rating +
                    '}';
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) return false;
            Problem pr = (Problem) obj;
            return this.code.equals(pr.code) && this.name.equals(pr.name) &&
                    this.url.equals(pr.url) && this.rating.equals(pr.rating) &&
                    (this.accepted == pr.accepted);
        }
    }

    /**
     * Default initial list
     */
    public void get(AppThreader.EventListener<List<Problem>> listener) {getProblemList(listener);}

    /**
     * List with changed difficulty
     */
    public void changeDifficulty(int difficulty, AppThreader.EventListener<List<Problem>> listener) {
        maxDifficulty = Math.min(difficulty, 3500);
        minDifficulty = maxDifficulty == 800 ? 0 : maxDifficulty - 99;
        page = 1;
        getProblemList(listener);
    }

    public void nextPage(AppThreader.EventListener<List<Problem>> listener) {
        ++page;
        getProblemList(listener);
    }

    public void prevPage(AppThreader.EventListener<List<Problem>> listener) {
        if (page >= 2) --page;
        getProblemList(listener);
    }

    public void reset() {
        minDifficulty = 0;
        maxDifficulty = 800;
        page = 1;
    }

    public int getPage() {return page;}

    public int revertPage() {return page >= 2 ? --page : page;}

    /**
     * Function that parses the HTML to get list of problems
     */
    private void getProblemList(AppThreader.EventListener<List<Problem>> listener) {
        NetworkClient.get().ReqGet(getURL(), body -> {
            List<Problem> arr = new ArrayList<>();

            Elements problems = body.select("table.problems").select("tr");
            for (int i = 1; i < problems.size(); i++) {
                ProblemParser.Problem pr = new ProblemParser.Problem();
                Element problem = problems.get(i);

                Element pID = problem.select("td.id").select("a").first(),
                        pName = problem.select("td").select("div").select("a").first(),
                        pRating = problem.select("span.ProblemRating").first();
                if (pID == null || pName == null || pRating == null) continue;

                pr.accepted = problem.hasClass("accepted-problem");
                pr.code = pID.text().trim();
                pr.name = pName.text().trim();
                pr.rating = pRating.text().trim();
                pr.url = problem.select("td").select("div").select("a").attr("href").trim();
                arr.add(pr);
            }
            listener.onEvent(arr);
        });
    }

    /**
     * @return URL with {@link #page}, {@link #minDifficulty}, {@link #maxDifficulty}
     */
    private String getURL() {
        return "https://codeforces.com/problemset/page/" + page + "?tags=" +
                minDifficulty + "-" + maxDifficulty + "&order=BY_RATING_ASC";
    }

    /**
     * Get HTML of the question
     */
    public static String getQuestion(String url, String code) {
        if (code != null) {
            JSONArray probArr = AppData.get().getData(AppData.DOWNLOADED_QUES, new JSONArray());
            for (Object obj : probArr) {
                JSONObject jObj = (JSONObject) obj;
                if (jObj.getString(AppData.P_CODE_KEY).equals(code)) {
                    if (!hasError(Jsoup.parse(jObj.getString(AppData.P_HTML_KEY)))) {
                        System.out.println("Returning downloaded");
                        return jObj.getString(AppData.P_HTML_KEY);
                    }
                }
            }
        }
        try {
            Document doc = Jsoup.connect(url).cookies(NetworkClient.get().getCookies()).data(NetworkClient.get().getParams()).followRedirects(true).execute().parse();
            doc.outputSettings(doc.outputSettings().prettyPrint(false).escapeMode(Entities.EscapeMode.extended).charset("ASCII"));
            return doc.html().replaceAll("//codeforces.org", "https://codeforces.org");
        } catch (IOException e) {
            e.printStackTrace();
            return "<h2>Unable to load page</h2>";
        }
    }

    /**
     * Removes some elements
     */
    public static String trimHTML(String html) {
        Document doc = Jsoup.parse(html);
        doc.select("div#header").remove();
        doc.select("div.roundbox.menu-box").remove();

        Element sidebar = doc.select("div#sidebar").first();
        if (sidebar != null) {
            for (Element ele : sidebar.children()) {
                if (ele.hasClass("roundbox") && ele.hasClass("sidebox") &&
                        ele.select("div.caption.titled").text().contains("submissions")) continue;
                ele.remove();
            }
        }
        doc.select("div.second-level-menu").remove();
        doc.select("div#footer").remove();
        doc.outputSettings(doc.outputSettings().prettyPrint(false).escapeMode(Entities.EscapeMode.extended).charset("ASCII"));
        return doc.html();
    }

    public static boolean hasError(Document doc) {
        try {
            Element ele = doc.select("h2").first();
            return ele != null && ele.text().equals("Unable to load page");
        } catch (Exception e) {
            return false;
        }
    }

    public ProblemParser() {}
}
