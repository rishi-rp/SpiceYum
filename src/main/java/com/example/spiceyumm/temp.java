package com.example.spiceyumm;

import java.sql.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.*;
import org.json.simple.parser.*;

class temp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/MealDB";
    private static final String USER = "root";
    private static final String PASS = "password";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            fetchCategories(conn);
            fetchAllMeals(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fetchCategories(Connection conn) throws Exception {
        String json = fetchJSON("https://www.themealdb.com/api/json/v1/1/categories.php");
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);
        JSONArray categories = (JSONArray) jsonObject.get("categories");

        String sql = "INSERT INTO categories (id, name, description, thumbnail) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object category : categories) {
                JSONObject cat = (JSONObject) category;
                ps.setInt(1, Integer.parseInt((String) cat.get("idCategory")));
                ps.setString(2, (String) cat.get("strCategory"));
                ps.setString(3, (String) cat.get("strCategoryDescription"));
                ps.setString(4, (String) cat.get("strCategoryThumb"));
                ps.executeUpdate();
            }
        }
    }

    private static void fetchAllMeals(Connection conn) throws Exception {
        for (char letter = 'a'; letter <= 'z'; letter++) {
            String json = fetchJSON("https://www.themealdb.com/api/json/v1/1/search.php?f=" + letter);
            if (json == null) continue;

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);
            JSONArray meals = (JSONArray) jsonObject.get("meals");
            if (meals == null) continue;

            String sql = "INSERT INTO meals (id, name, category, area, instructions, thumbnail, youtube, source) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=name";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Object meal : meals) {
                    JSONObject m = (JSONObject) meal;
                    ps.setInt(1, Integer.parseInt((String) m.get("idMeal")));
                    ps.setString(2, (String) m.get("strMeal"));
                    ps.setString(3, (String) m.get("strCategory"));
                    ps.setString(4, (String) m.get("strArea"));
                    ps.setString(5, (String) m.get("strInstructions"));
                    ps.setString(6, (String) m.get("strMealThumb"));
                    ps.setString(7, (String) m.get("strYoutube"));
                    ps.setString(8, (String) m.get("strSource"));
                    ps.executeUpdate();
                }
            }
        }
    }

    private static String fetchJSON(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
}
