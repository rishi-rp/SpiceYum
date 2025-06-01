package com.example.spiceyumm;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.sql.*;

public class HelloApplication extends Application {
    private Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/userdb", "root", "password");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerUser(String username, String password, String email, Label messageLabel) {
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and Password are required!");
            messageLabel.setTextFill(Color.RED);
            return;
        }
        try (Connection conn = connectDB()) {
            if (conn != null) {
                String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                if (email.isEmpty()) {
                    pstmt.setNull(3, Types.VARCHAR);
                } else {
                    pstmt.setString(3, email);
                }
                pstmt.executeUpdate();
                messageLabel.setText("Registration successful!");
                messageLabel.setTextFill(Color.GREEN);
            }
        } catch (SQLException e) {
            messageLabel.setText("User already exists!");
            messageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    private int loginUser(String username, String password, Label messageLabel) {
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Username and Password are required!");
            messageLabel.setTextFill(Color.RED);
            return -1; // Indicate login failure
        }
        try (Connection conn = connectDB()) {
            if (conn != null) {
                String query = "SELECT id FROM users WHERE username = ? AND password = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id"); // Return the User ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Indicate login failure
    }


    @Override
    public void start(Stage primaryStage) {
        Label userLabel = new Label("Username:");
        userLabel.setFont(Font.font(16));
        TextField userField = new TextField();
        userField.setMaxWidth(250);
        userField.setStyle("-fx-padding: 10px; -fx-border-color: #2196F3; -fx-border-radius: 5px;");

        Label passLabel = new Label("Password:");
        passLabel.setFont(Font.font(16));
        PasswordField passField = new PasswordField();
        passField.setMaxWidth(250);
        passField.setStyle("-fx-padding: 10px; -fx-border-color: #2196F3; -fx-border-radius: 5px;");

        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font(16));
        TextField emailField = new TextField();
        emailField.setMaxWidth(250);
        emailField.setStyle("-fx-padding: 10px; -fx-border-color: #2196F3; -fx-border-radius: 5px;");
        emailLabel.setVisible(false);
        emailField.setVisible(false);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px;");

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px 20px; -fx-background-radius: 5px;");

        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);
        messageLabel.setFont(Font.font(14));

        loginButton.setOnAction(e -> {
            int userId = loginUser(userField.getText(), passField.getText(), messageLabel);
            if (userId != -1) { // Successful login
                messageLabel.setText("Login successful!");
                messageLabel.setTextFill(Color.GREEN);
                primaryStage.close();

                // Pass the user ID to the next screen
                Stage recipeStage = new Stage();
                first recipeApp = new first(userId); // Pass user ID
                recipeApp.start(recipeStage);
                recipeStage.setMaximized(true);
            } else {
                messageLabel.setText("Invalid credentials!");
                messageLabel.setTextFill(Color.RED);
            }
        });


        registerButton.setOnAction(e -> {
            if (!emailLabel.isVisible()) {
                emailLabel.setVisible(true);
                emailField.setVisible(true);
            } else {
                registerUser(userField.getText(), passField.getText(), emailField.getText(), messageLabel);
            }
        });

        VBox layout = new VBox(15, userLabel, userField, passLabel, passField, emailLabel, emailField, loginButton, registerButton, messageLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ECEFF1; -fx-border-radius: 10px; -fx-padding: 20px;");

        Scene scene = new Scene(layout, 400, 500);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                primaryStage.setWidth(500);
                primaryStage.setHeight(500);
                primaryStage.centerOnScreen();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Login/Register");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
