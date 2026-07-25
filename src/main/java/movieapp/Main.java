package movieapp;

import movieapp.db.DatabaseConfig;
import movieapp.db.SchemaInitializer;
import movieapp.db.SampleDataSeeder;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            SchemaInitializer.initialize(conn);
            SampleDataSeeder.seed(conn);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    } // end of main
} // end of class