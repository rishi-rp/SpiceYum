package com.example.spiceyumm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.util.List;

public class dataset{

    private static final String CSV_FILE = "src/main/resources/IndianFoodDatasetCSV.csv";  // Update with your actual file path
    private static final String DB_URL = "jdbc:mysql://localhost:3306/MealDB?useUnicode=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root";   // Update your MySQL username
    private static final String DB_PASSWORD = "password";  // Update your MySQL password

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            List<String[]> records = readCSV(CSV_FILE);  // Read CSV
            insertDataIntoDatabase(conn, records);  // Insert into DB
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String[]> readCSV(String filePath) throws IOException, CsvException {
        try (Reader reader = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {
            return csvReader.readAll();  // Read all rows
        }
    }


    private static void insertDataIntoDatabase(Connection conn, List<String[]> records) throws SQLException {
        String sql = "INSERT INTO meals (id, name, area, category, instructions, thumbnail) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), area = VALUES(area), category = VALUES(category), " +
                "instructions = VALUES(instructions), thumbnail = VALUES(thumbnail)";


        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 1; i < records.size(); i++) {  // Skip header row
                String[] row = records.get(i);

                // Fetch category value
                String category = row[10];  // Column 11 (category)

                // Check if the category exists in the categories table
                String checkCategorySQL = "SELECT COUNT(*) FROM categories WHERE name = ?";
                try (PreparedStatement checkCategoryStmt = conn.prepareStatement(checkCategorySQL)) {
                    checkCategoryStmt.setString(1, category);

                    try (ResultSet rs = checkCategoryStmt.executeQuery()) {
                        rs.next();
                        int categoryCount = rs.getInt(1);

                        // If category exists, insert into meals, else set category to NULL
                        if (categoryCount > 0) {
                            pstmt.setInt(1, Integer.parseInt(row[0]));  // ID from column 1
                            pstmt.setString(2, row[1]);  // Name from column 2
                            pstmt.setString(3, row[9]);  // Area from column 10
                            pstmt.setString(4, category);  // Category from column 11
                            pstmt.setString(5, row[12]);  // Instructions from column 13
                            pstmt.setString(6, row[14]);  // Thumbnail from column 15
                        } else {
                            pstmt.setInt(1, Integer.parseInt(row[0]));  // ID from column 1
                            pstmt.setString(2, row[1]);  // Name from column 2
                            pstmt.setString(3, row[9]);  // Area from column 10
                            pstmt.setNull(4, Types.VARCHAR);  // Set category to NULL
                            pstmt.setString(5, row[12]);  // Instructions from column 13
                            pstmt.setString(6, row[14]);  // Thumbnail from column 15
                        }
                        pstmt.addBatch();
                    }
                } catch (SQLException ex) {
                    System.out.println("Error checking category: " + ex.getMessage());
                }
            }

            pstmt.executeBatch();
            System.out.println("Data successfully inserted into MealDB!");
        }
    }

}
