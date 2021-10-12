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

import com.shank.offcoder.app.NetworkClient;

import java.util.ArrayList;
import java.util.List;

public class ProblemSetHandler {

    private int minDifficulty = 0, maxDifficulty = 800, page = 1;

    /**
     * Data class for problems
     */
    public static class Problem {
        public String code, name, url, rating;
        public boolean accepted = false;

        public Problem() {
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
     * @return Default initial list
     */
    public List<Problem> get() {
        return getProblemList();
    }

    /**
     * @return list with changed difficulty
     */
    public List<Problem> changeDifficulty(int difficulty) {
        maxDifficulty = Math.min(difficulty, 3500);
        minDifficulty = maxDifficulty == 800 ? 0 : maxDifficulty - 99;
        page = 1;
        return getProblemList();
    }

    public List<Problem> nextPage() {
        ++page;
        return getProblemList();
    }

    public List<Problem> prevPage() {
        if (page >= 2) --page;
        return getProblemList();
    }

    public void reset() {
        minDifficulty = 0;
        maxDifficulty = 800;
        page = 1;
    }

    public int getPage() {
        return page;
    }

    public int revertPage() {
        return page >= 2 ? --page : page;
    }

    /**
     * Function that parses the HTML to get list of problems
     *
     * @return list of problems
     */
    private List<Problem> getProblemList() {
        List<Problem> arr = new ArrayList<>();

        Codeforces.login(Codeforces.HANDLE, Codeforces.PASS);
        String body = NetworkClient.ReqGet(getURL());
        String[] lines = body.split("\n");

        Problem pr = new Problem();
        boolean problemStarted = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.equals("<tr class=\"accepted-problem\">") || line.equals("<tr>")) {
                problemStarted = i <= lines.length - 1 && lines[i + 1].trim().equals("<td class=\"id\">");
                if (problemStarted && !pr.accepted) {
                    pr.accepted = line.equals("<tr class=\"accepted-problem\">");
                    continue;
                }
            }
            if (problemStarted) {
                if (line.equals("<td class=\"id\">")) {
                    pr.code = lines[i + 2].trim();
                    pr.url = lines[i + 1].trim().replace("<a href=\"", "").replace(">", "").trim();
                    i += 2;
                    continue;
                }
                if (line.equals("<div style=\"float: left;\">")) {
                    pr.name = lines[i + 2].replace("</a>", "").trim();
                    i += 2;
                    continue;
                }
                if (line.equals("<td style=\"font-size: 1.1rem\">")) {
                    String str_rating = lines[i + 1].trim();
                    StringBuilder number = new StringBuilder();
                    for (char c : str_rating.toCharArray()) {
                        if (c >= 48 && c <= 57) number.append(c);
                    }
                    ++i;
                    pr.rating = number.toString();
                    continue;
                }
                if (line.equals("</tr>")) {
                    problemStarted = false;
                    arr.add(pr);
                    pr = new Problem();
                }
            }
        }
        return arr;
    }

    /**
     * @return URL with {@link #page}, {@link #minDifficulty}, {@link #maxDifficulty}
     */
    private String getURL() {
        return "https://codeforces.com/problemset/page/" + page + "?tags=" +
                minDifficulty + "-" + maxDifficulty + "&order=BY_RATING_ASC";
    }

    public ProblemSetHandler() {
    }
}
