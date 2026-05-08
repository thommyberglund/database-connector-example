package eu.frosteby;

/**
 * Configuration class for test database containers.
 * Provides connection details for Testcontainers-based tests.
 */
public class TestDatabaseConfig {
    
    // MySQL Test Container
    public static final String MYSQL_IMAGE = "mysql:8.3";
    public static final String MYSQL_DATABASE = "test_db";
    public static final String MYSQL_USERNAME = "testuser";
    public static final String MYSQL_PASSWORD = "testpass";
    
    // PostgreSQL Test Container
    public static final String POSTGRESQL_IMAGE = "postgres:16";
    public static final String POSTGRESQL_DATABASE = "test_db";
    public static final String POSTGRESQL_USERNAME = "testuser";
    public static final String POSTGRESQL_PASSWORD = "testpass";
    
    /**
     * Gets the JDBC URL for MySQL test container.
     * 
     * @param host the container host
     * @param port the container port
     * @return JDBC URL for MySQL
     */
    public static String getMySqlJdbcUrl(String host, int port) {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", 
            host, port, MYSQL_DATABASE);
    }
    
    /**
     * Gets the JDBC URL for PostgreSQL test container.
     * 
     * @param host the container host
     * @param port the container port
     * @return JDBC URL for PostgreSQL
     */
    public static String getPostgreSqlJdbcUrl(String host, int port) {
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, POSTGRESQL_DATABASE);
    }
}
