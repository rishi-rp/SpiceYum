package com.example.spiceyumm;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class first extends Application {
    private ListView<HBox> recipeList;
    private TextField searchField;
    private VBox topPicksSection;
    private VBox sidebar;
    private DoubleProperty sidebarWidth = new SimpleDoubleProperty(200); // Default sidebar width
    private Stage primaryStage;
    private boolean sidebarVisible = false;
    private Label topPicksLabel;
    private int loggedInUserId;
    private BorderPane layout;
    private boolean largeTextOn = false;



    private static final String DEFAULT_IMAGE = "https://via.placeholder.com/150"; // Default thumbnail

    public first() {
        // Default constructor for JavaFX
    }


    public first(int userId) {  // Constructor to receive user ID
        this.loggedInUserId = userId;
        System.out.println("User Logged In ID: " + loggedInUserId); // Debugging
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage=primaryStage;
        // Top Picks Section
        topPicksSection = new VBox(10);
        topPicksSection.setAlignment(Pos.TOP_CENTER);
        topPicksSection.setPadding(new Insets(20));
        topPicksSection.getStyleClass().add("top-picks-section");



        Label topPicksLabel = new Label("RECOMMENDATION");
        this.topPicksLabel=topPicksLabel;
        topPicksLabel.getStyleClass().add("top-picks-label");

        recipeList = new ListView<>();
        recipeList.getStyleClass().add("recipe-list");
        loadRandomRecipes();

        topPicksSection.getChildren().addAll(topPicksLabel, recipeList);

        // Search Bar
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");
        Button searchButton = new Button("🔍");
        searchButton.getStyleClass().add("search-button");
        searchButton.setOnAction(e -> searchRecipes());




        // Navigation Bar
        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER);
        navBar.getStyleClass().add("nav-bar");

        Button homeButton = new Button("🏠");
        Button addButton = new Button("➕");
        addButton.setOnAction(e -> openAddMealDialog());
        HBox searchBox = new HBox(5, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.getStyleClass().add("search-box");
        Button menuButton = new Button("☰");
        Button chatbot=new Button("🤖" );

        chatbot.setOnAction(e -> openChatbot());
        menuButton.setOnAction(e -> toggleSidebar());
        // Sidebar Menu (Hidden Initially)

        sidebar = createSidebar();
        // Start hidden
        primaryStage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            // Move sidebar off-screen dynamically
            if (!sidebarVisible)
                sidebar.setTranslateX(600+(double)newWidth);
            else
                sidebar.setTranslateX(900+(double)newWidth);

        });




        homeButton.getStyleClass().add("nav-button");
        addButton.getStyleClass().add("nav-button");
        menuButton.getStyleClass().add("nav-button");
        chatbot.getStyleClass().add("nav-button");

        homeButton.setOnAction(e -> loadRandomRecipes());

        navBar.getChildren().addAll(homeButton, searchBox, addButton, menuButton,chatbot);



        // Layout
        BorderPane layout = new BorderPane();
        this.layout=layout;
        layout.setCenter(topPicksSection);
        layout.setBottom(navBar);

        StackPane root = new StackPane();
        root.getChildren().addAll(layout, sidebar);

        // Scene and Stage
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setTitle("Recipe App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private VBox createSidebar() {
        VBox sidebarMenu = new VBox(15);
        sidebarMenu.setPadding(new Insets(20));
        sidebarMenu.setAlignment(Pos.TOP_LEFT);
        sidebarMenu.setStyle("-fx-background-color: #333; -fx-pref-width: 200px; -fx-padding: 20px;");
        // Create Sidebar Menu
        sidebarMenu.setPrefWidth(300);
        sidebarMenu.setTranslateX(600); // Move it off-screen initially
        sidebarMenu.setStyle("-fx-background-color: white; -fx-border-color: black;");

        sidebarMenu.setMinWidth(300);
        sidebarMenu.setMaxWidth(300);

        // Initially hidden on the left side

        Label title = new Label("Menu");
        title.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");


        Button accountButton = new Button("👤 Account");
        Button settingsButton = new Button("⚙ Settings");

        settingsButton.setOnAction(e -> openSettingsPage());



        accountButton.setOnAction(e -> openProfilePage());

        Button yourRecipesButton = new Button("📜 Your Recipes");
        Button bookmarks=new Button("⭐ Bookmarks");
        yourRecipesButton.getStyleClass().add("sidebar-button");

        yourRecipesButton.setOnAction(e -> loadUserRecipes());
        Button closeButton = new Button("❌ Close");


        bookmarks.getStyleClass().add("sidebar-button");
        settingsButton.getStyleClass().add("sidebar-button");

        accountButton.getStyleClass().add("sidebar-button");
        closeButton.getStyleClass().add("sidebar-button");


        bookmarks.setOnAction(e-> {bookmarks();});
        closeButton.setOnAction(e -> {toggleSidebar();});

        sidebarMenu.getChildren().addAll(title, settingsButton, accountButton,yourRecipesButton,bookmarks, closeButton);
        return sidebarMenu;
    }
    private void openProfilePage() {
        VBox profileLayout = new VBox(10);
        profileLayout.setPadding(new Insets(20));
        profileLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Your Profile");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        String[] userProfile = fetchUserProfile(); // Fetch user details

        Label nameLabel = new Label("Name: " + userProfile[0]);
        Label emailLabel = new Label("Email: " + userProfile[1]);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> layout.setCenter(topPicksSection)); // Return to main content

        profileLayout.getChildren().addAll(titleLabel, nameLabel, emailLabel, backButton);

        layout.setCenter(profileLayout); // Update center with profile page
    }

    @FXML
    public void openChatbot() {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/spiceyumm/chatbot.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: /com/example/spiceyumm/chatbot.fxml");
                return; // Exit if file not found
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Chatbot");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void openSettingsPage() {
        VBox settingsLayout = new VBox(15);
        settingsLayout.setPadding(new Insets(20));
        settingsLayout.setAlignment(Pos.TOP_CENTER);

        Label settingsTitle = new Label("Settings");
        settingsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Dark Mode Toggle
        Button darkModeButton = new Button("🌙 Toggle Dark Mode");
        darkModeButton.setOnAction(e -> toggleDarkMode());

        // Large Text Toggle
        Button largeTextButton = new Button("🔠 Toggle Large Text");
        largeTextButton.setOnAction(e -> toggleLargeText(largeTextButton.getScene()));

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> layout.setCenter(topPicksSection)); // Go back to main page

        settingsLayout.getChildren().addAll(settingsTitle, darkModeButton, largeTextButton, backButton);

        layout.setCenter(settingsLayout);
    }


    private void toggleDarkMode() {
        Scene scene = primaryStage.getScene();
        if (scene.getStylesheets().contains(getClass().getResource("/style.css").toExternalForm())) {
            scene.getStylesheets().clear();

            scene.getStylesheets().add(getClass().getResource("/darkmode.css").toExternalForm());
        } else {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
    }

    private void toggleLargeText(Scene scene) {
        if (!largeTextOn) {
            scene.getRoot().setStyle("-fx-font-size: 18px;");
        } else {
            scene.getRoot().setStyle("-fx-font-size: 12px;");
        }
        largeTextOn = !largeTextOn;
    }






    // Fetch user profile from database
    private String[] fetchUserProfile() {
        if (loggedInUserId == -1) {
            return new String[]{"Not Logged In", "N/A"}; // No user logged in
        }

        String url = "jdbc:mysql://localhost:3306/userdb";
        String dbUser = "root";
        String dbPassword = "password";
        String query = "SELECT username, email FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, loggedInUserId);  // Use logged-in user ID
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new String[]{rs.getString("username"), rs.getString("email")};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new String[]{"Unknown", "Unknown"};
    }







    private void toggleSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sidebar);

        // Move sidebar off-screen dynamically
        if (sidebarVisible) {
            slide.setToX(1200); // Hide sidebar
        } else {
            slide.setToX(0.4*primaryStage.getWidth()); // Show sidebar without covering content
        }


        slide.play();
        sidebarVisible = !sidebarVisible;
    }
    private void bookmarks() {
        topPicksLabel.setText("Bookmarks");
        recipeList.getItems().clear();
        List<String[]> recipes=fetchRecipes("SELECT name, '' AS category, instructions, NULL AS thumbnail FROM bookmark");

        for(String[] recipe: recipes){
            recipeList.getItems().add(createRecipeItem(recipe));
        }
    }
    private void loadUserRecipes() {
        topPicksLabel.setText("Your Recipes");
        recipeList.getItems().clear();
        List<String[]> recipes = fetchRecipes("SELECT name, '' AS category, instructions, NULL AS thumbnail FROM user_meals");

        for (String[] recipe : recipes) {
            recipeList.getItems().add(createRecipeItem(recipe));
        }
    }


    private void openAddMealDialog() {
        Stage addMealStage = new Stage();
        addMealStage.setTitle("Add New Meal");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter meal name");

        TextArea instructionsArea = new TextArea();
        instructionsArea.setPromptText("Enter instructions...");
        instructionsArea.setWrapText(true);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            String instructions = instructionsArea.getText().trim();

            if (!name.isEmpty() && !instructions.isEmpty()) {
                saveMealToDatabase(name, instructions);
                addMealStage.close();
            } else {
                showAlert("Error", "Name and Instructions cannot be empty.");
            }
        });

        layout.getChildren().addAll(nameField, instructionsArea, saveButton);

        Scene scene = new Scene(layout, 300, 250);
        addMealStage.setScene(scene);
        addMealStage.show();
    }

    private void saveMealToDatabase(String name, String instructions) {
        String url = "jdbc:mysql://localhost:3306/MealDB";
        String user = "root";
        String password = "password";

        String query = "INSERT INTO user_meals (name, instructions) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, instructions);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                showAlert("Success", "Meal added successfully!");
                loadRandomRecipes(); // Refresh the list
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to insert meal.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadRandomRecipes() {
        topPicksLabel.setText("RECOMMENDED");
        recipeList.getItems().clear();
        List<String[]> recipes = fetchRecipes("SELECT * FROM meals WHERE id BETWEEN 52764 AND 53083 ORDER BY RAND() LIMIT 5;");

        for (String[] recipe : recipes) {
            recipeList.getItems().add(createRecipeItem(recipe));
        }
    }

    private void searchRecipes() {
        String keyword = searchField.getText().trim();
        recipeList.getItems().clear();
        List<String[]> recipes = fetchRecipes("SELECT * FROM meals WHERE name LIKE ?", "%" + keyword + "%");

        for (String[] recipe : recipes) {
            recipeList.getItems().add(createRecipeItem(recipe));
        }
    }

    private List<String[]> fetchRecipes(String query, String... params) {
        List<String[]> recipes = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/MealDB";
        String user = "root";
        String password = "password";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (params.length > 0) {
                stmt.setString(1, params[0]);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                recipes.add(new String[]{
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("instructions"),
                        rs.getString("thumbnail")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipes;
    }
//    private void showRecipeDetails(String name, String category, String instructions, String thumbnailUrl) {
//        Stage detailsStage = new Stage();
//        VBox layout = new VBox(10);
//        layout.setPadding(new Insets(20));
//        layout.setAlignment(Pos.CENTER);
//
//        Label nameLabel = new Label("Name: " + name);
//        Label categoryLabel = new Label("Category: " + (category != null ? category : "Unknown"));
//
//        TextArea instructionsArea = new TextArea(instructions);
//        instructionsArea.setWrapText(true);
//        instructionsArea.setEditable(false);
//
//        // If thumbnail URL is null, use default image
//
//        String imageUrl = (thumbnailUrl != null && !thumbnailUrl.isEmpty()) ? thumbnailUrl : DEFAULT_IMAGE;
//        ImageView imageView = new ImageView(new Image(imageUrl, 200, 200, true, true));
//        imageView.setPreserveRatio(true);
//        Button bookmarkButton = new Button("⭐ Bookmark");
//        bookmarkButton.setOnAction(e -> addToBookmarks(name, instructions));
//
//        layout.getChildren().addAll(imageView, nameLabel, categoryLabel, instructionsArea,bookmarkButton);
//
//        Scene scene = new Scene(layout, 400, 400);
//        detailsStage.setTitle(name);
//        detailsStage.setScene(scene);
//        detailsStage.show();
//    }
private void showRecipeDetails(String name, String category, String instructions, String thumbnailUrl) {
    VBox detailLayout = new VBox(15);
    detailLayout.setPadding(new Insets(20));
    detailLayout.setAlignment(Pos.TOP_CENTER);

    // Image
    String imageUrl = (thumbnailUrl != null && !thumbnailUrl.isEmpty()) ? thumbnailUrl : DEFAULT_IMAGE;
    ImageView imageView = new ImageView(new Image(imageUrl, 250, 250, true, true));
    imageView.setPreserveRatio(true);

    // Labels
    Label nameLabel = new Label(name);
    nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

    Label categoryLabel = new Label("Category: " + (category != null && !category.isEmpty() ? category : "Unknown"));
    categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");


    // Instructions
    TextArea instructionsArea = new TextArea(instructions);
    instructionsArea.setWrapText(true);
    instructionsArea.setEditable(false);
    instructionsArea.setStyle("-fx-font-size: 14px;");
    instructionsArea.setPrefHeight(200);

    // Buttons
    Button bookmarkButton = new Button("⭐ Bookmark");
    bookmarkButton.setOnAction(e -> addToBookmarks(name, instructions));

    Button backButton = new Button("🔙 Back");
    backButton.setOnAction(e -> layout.setCenter(topPicksSection));

    HBox buttonBox = new HBox(10, bookmarkButton, backButton);
    buttonBox.setAlignment(Pos.CENTER);

    detailLayout.getChildren().addAll(imageView, nameLabel, categoryLabel, instructionsArea, buttonBox);
    layout.setCenter(detailLayout);
}


    private void addToBookmarks(String name,String instructions) {
        String url = "jdbc:mysql://localhost:3306/MealDB";
        String user = "root";
        String password = "password";

        String checkQuery = "SELECT COUNT(*) FROM bookmark WHERE name = ?";
        String insertQuery = "INSERT INTO bookmark (name, instructions) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            checkStmt.setString(1, name);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                showAlert("Info", "This recipe is already bookmarked!");
                return;
            }

            insertStmt.setString(1, name);
            insertStmt.setString(2, instructions);
            int rowsInserted = insertStmt.executeUpdate();

            if (rowsInserted > 0) {
                showAlert("Success", "Recipe bookmarked successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to bookmark the recipe.");
        }
    }


    private HBox createRecipeItem(String[] recipe) {
        String name = recipe[0];
        String category = recipe[1];
        String instructions = recipe[2];
        String thumbnailUrl = recipe[3] != null ? recipe[3] : DEFAULT_IMAGE; // Fix: Use default if NULL

        ImageView imageView = new ImageView(new Image(thumbnailUrl, 80, 80, true, true));
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("recipe-name");

        VBox textContainer = new VBox(nameLabel);
        textContainer.setPadding(new Insets(5));

        HBox recipeBox = new HBox(10, imageView, textContainer);
        recipeBox.setAlignment(Pos.CENTER_LEFT);
        recipeBox.setOnMouseClicked(e -> showRecipeDetails(name, category, instructions, thumbnailUrl));

        return recipeBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
