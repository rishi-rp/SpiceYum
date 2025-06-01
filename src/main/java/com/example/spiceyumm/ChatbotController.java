package com.example.spiceyumm;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatbotController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField userInputField;
    @FXML
    private Button sendButton;

    // Database connection details
    private String url = "jdbc:mysql://localhost:3306/MealDB";
    private String user = "root";
    private String password = "password";  // Replace with your actual password.  Don't commit passwords in code.

    private static final String GEMINI_API_KEY = "YOUR_API_KEY"; // Replace with your actual API key
    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta2/models/chat-bison-001:generateMessage?key=" + GEMINI_API_KEY;

    @FXML
    private void handleSendMessage() {
        String userInput = userInputField.getText().trim();
        if (!userInput.isEmpty()) {
            chatArea.appendText("You: " + userInput + "\n");
            String response = processUserInput(userInput);
            chatArea.appendText("Bot: " + response + "\n");
            userInputField.clear();
        }
    }

    private String processUserInput(String userInput) {
        // Improved logic:  Check for simple greetings first.
        String greetingResponse = handleGreeting(userInput);
        if (greetingResponse != null) {
            return greetingResponse; // Return the greeting
        }

        // Try Gemini, then fall back to the database.
        String geminiResponse = getGeminiResponse(userInput);
        if (geminiResponse != null && !geminiResponse.startsWith("Error:")) {
            return geminiResponse;
        }

        String recipeName = extractRecipeName(userInput);
        String databaseResponse = getRecipeFromDatabase(recipeName);
        if (databaseResponse != null) {
            return databaseResponse;
        }

        return "I'm having trouble finding that information right now.";
    }

    private String handleGreeting(String userInput) {
        String lowerCaseInput = userInput.toLowerCase();
        if (lowerCaseInput.contains("hi") || lowerCaseInput.contains("hello") || lowerCaseInput.contains("hey")) {
            return "Hello! How can I help you today?  I can find recipe information, or you can ask me anything!";
        }
        return null; // Not a greeting.
    }

    private String getGeminiResponse(String userMessage) {
        if (GEMINI_API_KEY == null || GEMINI_API_KEY.equals("API_KEY")) {
            return "Error: Gemini API key is not set.  Please configure it in the code.";
        }

        try {
            // Construct the JSON payload.  Use a library for more robust JSON.
            String jsonInput = String.format(
                    "{ \"prompt\": { \"text\": \"%s\" }, \"temperature\": 0.7, \"max_output_tokens\": 200, \"top_p\": 1.0, \"top_k\": 40 }",
                    userMessage.replace("\"", "\\\"") // Escape double quotes in the user message
            );

            // Create the URL object for the endpoint
            URL url = new URL(GEMINI_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Set the request method and headers
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept-Charset", "UTF-8"); // Ensure UTF-8 encoding

            // Sending JSON to the API, using UTF-8 encoding
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Handle the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line.trim());
                    }

                    String fullResponse = response.toString();
                    System.out.println("Full Response: " + fullResponse); // Debugging

                    // Use a more robust JSON parsing method (if needed, for complex responses)
                    Pattern pattern = Pattern.compile("\"content\":\\s*\"(.*?)\"");  // Correct pattern.
                    Matcher matcher = pattern.matcher(fullResponse);

                    if (matcher.find()) {
                        String extractedText = matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
                        return extractedText;
                    }
                    else
                    {
                        return "I couldn't understand the reply from the AI. Please try again.";
                    }
                }
            } else {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    String errorMessage = errorResponse.toString();
                    System.out.println("Error Response: " + errorMessage);
                    return "Error: HTTP " + responseCode + " - " + errorMessage; // Include error message
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error contacting Gemini: " + e.getMessage();
        }
    }

    private String extractRecipeName(String input) {
        String[] keywords = {"for", "of", "make", "cook", "prepare", "recipe for"};
        for (String keyword : keywords) {
            if (input.toLowerCase().contains(keyword)) {
                int index = input.toLowerCase().indexOf(keyword) + keyword.length();
                return input.substring(index).trim();
            }
        }
        // fallback: return last 2 words
        String[] words = input.split(" ");
        if (words.length >= 2) {
            return words[words.length - 2] + " " + words[words.length - 1];
        }
        if(words.length > 0)
            return words[words.length - 1]; // last word fallback
        return "";
    }

    private String getRecipeFromDatabase(String recipeName) {
        String query = "SELECT name, instructions, category, thumbnail FROM meals WHERE name LIKE ?";
        List<String> recipeDetails = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + recipeName + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                recipeDetails.add("Recipe: " + rs.getString("name"));
                recipeDetails.add("Category: " + rs.getString("category"));
                recipeDetails.add("Instructions: " + rs.getString("instructions"));
                String imageUrl = rs.getString("thumbnail");
                recipeDetails.add("Thumbnail: " + (imageUrl != null ? imageUrl : "No image available"));
                return String.join("\n", recipeDetails);
            } else {
                return null; // Return null if not found, to indicate no DB result
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error fetching recipe details: " + e.getMessage();
        }
    }
}
