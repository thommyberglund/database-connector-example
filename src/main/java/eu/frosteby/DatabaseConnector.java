package eu.frosteby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC Database Connector class.
 * Provides methods to establish and manage database connections.
 */
public class DatabaseConnector {
    
    private Connection connection;
    
    /**
     * Creates a new DatabaseConnector instance.
     * Connection is not established until connect() is called.
     */
    public DatabaseConnector() {
        this.connection = null;
    }
    
    /**
     * Establishes a connection to the database using configuration from DatabaseConfig.
     * 
     * @return true if connection was successful, false otherwise
     * @throws SQLException if a database access error occurs
     * @throws IllegalStateException if required configuration is missing
     */
    public boolean connect() throws SQLException {
        DatabaseConfig.validate();
        
        String jdbcUrl = DatabaseConfig.getJdbcUrl();
        String username = DatabaseConfig.getUsername();
        String password = DatabaseConfig.getPassword();
        int timeout = DatabaseConfig.getConnectionTimeout();
        
        try {
            // Set connection timeout
            DriverManager.setLoginTimeout(timeout);
            
            // Establish connection
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
            
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Closes the database connection if it is open.
     * 
     * @throws SQLException if a database access error occurs
     */
    public void disconnect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
        }
    }
    
    /**
     * Checks if the connection is currently open and valid.
     * 
     * @return true if connection is open and valid, false otherwise
     */
    public boolean isConnected() {
        try {
            return this.connection != null && !this.connection.isClosed() && this.connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Gets the current database connection.
     * 
     * @return the Connection object
     * @throws IllegalStateException if not connected
     */
    public Connection getConnection() {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected to database. Call connect() first.");
        }
        return this.connection;
    }
    
    /**
     * Executes a simple test query to verify the connection is working.
     * 
     * @return true if test query succeeds, false otherwise
     */
    public boolean testConnection() {
        try {
            if (!isConnected()) {
                connect();
            }
            
            // Execute a simple query that should work on most databases
            String testQuery = getTestQuery();
            try (var statement = this.connection.createStatement();
                 var resultSet = statement.executeQuery(testQuery)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets a simple test query appropriate for the configured database type.
     * 
     * @return test query string
     */
    private String getTestQuery() {
        String dbType = DatabaseConfig.getDbType();
        switch (dbType) {
            case "postgresql":
                return "SELECT 1";
            case "mysql":
            default:
                return "SELECT 1";
        }
    }
    
    /**
     * Creates a new DatabaseConnector, connects, executes a callback, and disconnects.
     * Useful for one-off database operations.
     * 
     * @param callback the operation to execute with the connection
     * @param <T> the return type of the callback
     * @return the result of the callback
     * @throws SQLException if a database access error occurs
     */
    public static <T> T withConnection(ConnectionCallback<T> callback) throws SQLException {
        DatabaseConnector connector = new DatabaseConnector();
        try {
            connector.connect();
            return callback.doWithConnection(connector.getConnection());
        } finally {
            try {
                connector.disconnect();
            } catch (SQLException e) {
                // Log but don't throw to preserve original exception
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Functional interface for operations that use a Connection.
     * 
     * @param <T> the return type
     */
    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T doWithConnection(Connection connection) throws SQLException;
    }
}
