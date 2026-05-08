package eu.frosteby;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Main class demonstrating the usage of DatabaseConnector.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("Database Connector Example");
        System.out.println("==========================");
        
        // Display configuration
        displayConfiguration();
        
        // Test connection
        testConnection();
        
        // Example: Execute a simple query
        executeSimpleQuery();
    }
    
    /**
     * Displays the current database configuration.
     */
    private static void displayConfiguration() {
        System.out.println("\n[Configuration]");
        System.out.println("Database Type: " + DatabaseConfig.getDbType());
        System.out.println("Host: " + DatabaseConfig.getHost());
        System.out.println("Port: " + DatabaseConfig.getPort());
        System.out.println("JDBC URL: " + DatabaseConfig.getJdbcUrl());
        System.out.println("Connection Timeout: " + DatabaseConfig.getConnectionTimeout() + "s");
    }
    
    /**
     * Tests the database connection.
     */
    private static void testConnection() {
        System.out.println("\n[Testing Connection]");
        
        DatabaseConnector connector = new DatabaseConnector();
        try {
            boolean connected = connector.testConnection();
            if (connected) {
                System.out.println("✓ Connection test successful!");
            } else {
                System.out.println("✗ Connection test failed!");
            }
        } catch (Exception e) {
            System.err.println("✗ Connection test failed with exception: " + e.getMessage());
        }
    }
    
    /**
     * Executes a simple query to demonstrate database operations.
     */
    private static void executeSimpleQuery() {
        System.out.println("\n[Executing Simple Query]");
        
        try {
            // Use the withConnection helper for automatic connection management
            DatabaseConnector.withConnection(connection -> {
                // Get database metadata
                String dbProduct = connection.getMetaData().getDatabaseProductName();
                String dbVersion = connection.getMetaData().getDatabaseProductVersion();
                
                System.out.println("Database: " + dbProduct);
                System.out.println("Version: " + dbVersion);
                
                // Execute a simple query
                String query = getSampleQuery();
                System.out.println("Executing: " + query);
                
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(query)) {
                    
                    int columnCount = resultSet.getMetaData().getColumnCount();
                    System.out.println("Result has " + columnCount + " column(s)");
                    
                    if (resultSet.next()) {
                        System.out.println("✓ Query executed successfully!");
                        // Print first row
                        for (int i = 1; i <= columnCount; i++) {
                            System.out.println("  Column " + i + ": " + resultSet.getString(i));
                        }
                    }
                }
                
                return null;
            });
        } catch (SQLException e) {
            System.err.println("✗ Query execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Gets a sample query based on the database type.
     * 
     * @return sample query string
     */
    private static String getSampleQuery() {
        String dbType = DatabaseConfig.getDbType();
        switch (dbType) {
            case "postgresql":
                return "SELECT version()";
            case "mysql":
            default:
                return "SELECT VERSION() as version";
        }
    }
}
