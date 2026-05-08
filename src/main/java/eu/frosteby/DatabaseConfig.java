package eu.frosteby;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Utility class for loading database configuration from environment variables.
 * Uses dotenv-java to load variables from .env file.
 */
public class DatabaseConfig {
    
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    
    private static final String DEFAULT_DB_TYPE = "mysql";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_MYSQL_PORT = 3306;
    private static final int DEFAULT_POSTGRESQL_PORT = 5432;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 30;
    
    /**
     * Gets the database type from environment or returns default.
     * Supported values: mysql, postgresql
     * 
     * @return database type as string
     */
    public static String getDbType() {
        return dotenv.get("DB_TYPE", DEFAULT_DB_TYPE).toLowerCase();
    }
    
    /**
     * Gets the database host from environment or returns default.
     * 
     * @return database host
     */
    public static String getHost() {
        return dotenv.get("DB_HOST", DEFAULT_HOST);
    }
    
    /**
     * Gets the database port from environment or returns default based on DB type.
     * 
     * @return database port
     */
    public static int getPort() {
        String portStr = dotenv.get("DB_PORT");
        if (portStr != null && !portStr.isEmpty()) {
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                // Fall back to default
            }
        }
        return getDbType().equals("postgresql") ? DEFAULT_POSTGRESQL_PORT : DEFAULT_MYSQL_PORT;
    }
    
    /**
     * Gets the database name from environment.
     * 
     * @return database name
     * @throws IllegalStateException if DB_NAME is not set
     */
    public static String getDatabaseName() {
        String dbName = dotenv.get("DB_NAME");
        if (dbName == null || dbName.isEmpty()) {
            throw new IllegalStateException("DB_NAME environment variable is required");
        }
        return dbName;
    }
    
    /**
     * Gets the database username from environment.
     * 
     * @return database username
     * @throws IllegalStateException if DB_USER is not set
     */
    public static String getUsername() {
        String user = dotenv.get("DB_USER");
        if (user == null || user.isEmpty()) {
            throw new IllegalStateException("DB_USER environment variable is required");
        }
        return user;
    }
    
    /**
     * Gets the database password from environment.
     * 
     * @return database password
     * @throws IllegalStateException if DB_PASSWORD is not set
     */
    public static String getPassword() {
        String password = dotenv.get("DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException("DB_PASSWORD environment variable is required");
        }
        return password;
    }
    
    /**
     * Gets the connection timeout in seconds from environment.
     * 
     * @return connection timeout in seconds
     */
    public static int getConnectionTimeout() {
        String timeoutStr = dotenv.get("DB_CONNECTION_TIMEOUT");
        if (timeoutStr != null && !timeoutStr.isEmpty()) {
            try {
                return Integer.parseInt(timeoutStr);
            } catch (NumberFormatException e) {
                // Fall back to default
            }
        }
        return DEFAULT_CONNECTION_TIMEOUT;
    }
    
    /**
     * Gets the full JDBC connection URL based on configuration.
     * 
     * @return JDBC connection URL
     */
    public static String getJdbcUrl() {
        String dbType = getDbType();
        String host = getHost();
        int port = getPort();
        String dbName = getDatabaseName();
        
        switch (dbType) {
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
            case "mysql":
            default:
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, dbName);
        }
    }
    
    /**
     * Validates that all required environment variables are set.
     * 
     * @throws IllegalStateException if any required variable is missing
     */
    public static void validate() {
        getDatabaseName();
        getUsername();
        getPassword();
    }
}
