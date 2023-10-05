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

import com.shank.offcoder.cf.ProblemParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle personalized lists for user
 */
public class PersonalizedListManager {

    private PersonalizedListManager() {
    }

    /**
     * @return List of personalized lists created by user
     */
    public static List<String> getListsNames() {
        List<String> out = new ArrayList<>();

        JSONArray personalizedLists = AppData.get().getData(AppData.PERSONALIZED_LIST_KEY, new JSONArray());
        for (Object obj : personalizedLists) {
            JSONObject jObj = (JSONObject) obj;
            out.add(jObj.getString("name"));
        }
        return out;
    }

    /**
     * @param listName Name of the list to get questions from
     * @return List of questions from given list name
     */
    public static List<ProblemParser.Problem> getProblemList(String listName) {
        if (listName == null) return new ArrayList<>();
        if (listName.isEmpty()) return new ArrayList<>();

        List<ProblemParser.Problem> out = new ArrayList<>();
        JSONArray personalizedLists = AppData.get().getData(AppData.PERSONALIZED_LIST_KEY, new JSONArray());
        for (Object obj : personalizedLists) {
            JSONObject jObj = (JSONObject) obj;

            if (jObj.getString("name").equals(listName)) {
                JSONArray arr = jObj.getJSONArray("list");
                for (Object _obj : arr) {
                    JSONObject _jObj = (JSONObject) _obj;
                    ProblemParser.Problem pr = new ProblemParser.Problem(
                            _jObj.getString(AppData.P_CODE_KEY), _jObj.getString(AppData.P_NAME_KEY), _jObj.getString(AppData.P_URL_KEY),
                            _jObj.getString(AppData.P_RATING_KEY), _jObj.getBoolean(AppData.P_ACCEPTED_KEY)
                    );
                    out.add(pr);
                }
                break;
            }
        }
        return out;
    }

    /**
     * Method to add problems to personalized list
     *
     * @param listName Name of the list of to add to.
     * @param list     List of questions to add
     */
    public static void addToList(String listName, List<ProblemParser.Problem> list) {
        if (list == null) return;
        if (list.isEmpty()) return;
        if (listName == null) return;
        if (listName.isEmpty()) return;

        JSONArray personalizedLists = AppData.get().getData(AppData.PERSONALIZED_LIST_KEY, new JSONArray());
        for (Object obj : personalizedLists) {
            JSONObject jObj = (JSONObject) obj;

            if (jObj.getString("name").equals(listName)) {
                JSONArray arr = jObj.getJSONArray("list");
                for (ProblemParser.Problem p : list) {
                    if (arr.toString().contains(p.code)) continue;
                    arr.put(new JSONObject().put(AppData.P_CODE_KEY, p.code).put(AppData.P_NAME_KEY, p.name).put(AppData.P_URL_KEY, p.url)
                            .put(AppData.P_ACCEPTED_KEY, p.accepted).put(AppData.P_RATING_KEY, p.rating));
                }
                jObj.put("list", arr);
                break;
            }
        }
        AppData.get().writeData(AppData.PERSONALIZED_LIST_KEY, personalizedLists);
    }

    /**
     * Method to create a list and then add problems to it.
     * <p>
     * See {@link #addToList(String, List)}
     */
    public static void createList(String listName, List<ProblemParser.Problem> list) {
        if (list == null) return;
        if (list.isEmpty()) return;
        if (listName == null) return;
        if (listName.isEmpty()) return;

        if (listExists(listName)) {
            addToList(listName, list);
            return;
        }

        JSONArray arr = new JSONArray();
        for (ProblemParser.Problem p : list) {
            arr.put(new JSONObject().put(AppData.P_CODE_KEY, p.code).put(AppData.P_NAME_KEY, p.name).put(AppData.P_URL_KEY, p.url)
                    .put(AppData.P_ACCEPTED_KEY, p.accepted).put(AppData.P_RATING_KEY, p.rating));
        }

        JSONArray personalizedLists = AppData.get().getData(AppData.PERSONALIZED_LIST_KEY, new JSONArray());
        personalizedLists.put(new JSONObject().put("name", listName).put("list", arr));
        AppData.get().writeData(AppData.PERSONALIZED_LIST_KEY, personalizedLists);
    }

    /**
     * @param listName Name to search for
     * @return true if the given name already has a list
     */
    private static boolean listExists(String listName) {
        JSONArray personalizedLists = AppData.get().getData(AppData.PERSONALIZED_LIST_KEY, new JSONArray());
        for (Object obj : personalizedLists) {
            JSONObject jObj = (JSONObject) obj;
            if (jObj.getString("name").equals(listName)) return true;
        }
        return false;
    }
}
