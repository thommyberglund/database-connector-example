package eu.frosteby;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatabaseConnector with PostgreSQL Testcontainers.
 */
@Testcontainers
public class DatabaseConnectorPostgreSqlTest {
    
    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(TestDatabaseConfig.POSTGRESQL_IMAGE)
            .withDatabaseName(TestDatabaseConfig.POSTGRESQL_DATABASE)
            .withUsername(TestDatabaseConfig.POSTGRESQL_USERNAME)
            .withPassword(TestDatabaseConfig.POSTGRESQL_PASSWORD);
    
    @Test
    void testPostgreSqlConnection() throws SQLException {
        DatabaseConnector connector = new DatabaseConnector();
        
        try {
            // Connect using test container details
            String jdbcUrl = TestDatabaseConfig.getPostgreSqlJdbcUrl(
                postgresContainer.getHost(), 
                postgresContainer.getFirstMappedPort()
            );
            
            Connection connection = DriverManager.getConnection(
                jdbcUrl,
                TestDatabaseConfig.POSTGRESQL_USERNAME,
                TestDatabaseConfig.POSTGRESQL_PASSWORD
            );
            
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            
            connection.close();
        } finally {
            if (connector.isConnected()) {
                connector.disconnect();
            }
        }
    }
    
    @Test
    void testPostgreSqlQueryExecution() throws SQLException {
        String jdbcUrl = TestDatabaseConfig.getPostgreSqlJdbcUrl(
            postgresContainer.getHost(), 
            postgresContainer.getFirstMappedPort()
        );
        
        try (Connection connection = DriverManager.getConnection(
                jdbcUrl,
                TestDatabaseConfig.POSTGRESQL_USERNAME,
                TestDatabaseConfig.POSTGRESQL_PASSWORD)) {
            
            // Create a test table
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS test_table (id SERIAL PRIMARY KEY, name VARCHAR(100))");
                statement.execute("INSERT INTO test_table (name) VALUES ('Test Value')");
            }
            
            // Query the test table
            try (Statement statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT * FROM test_table WHERE name = 'Test Value'")) {
                
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt("id"));
                assertEquals("Test Value", resultSet.getString("name"));
            }
        }
    }
    
    @Test
    void testPostgreSqlWithDatabaseConnector() throws SQLException {
        String jdbcUrl = TestDatabaseConfig.getPostgreSqlJdbcUrl(
            postgresContainer.getHost(), 
            postgresContainer.getFirstMappedPort()
        );
        
        // Use the DatabaseConnector.withConnection helper
        DatabaseConnector.withConnection(connection -> {
            // Create a test table
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS test_table_2 (id SERIAL PRIMARY KEY, value INTEGER)");
                statement.execute("INSERT INTO test_table_2 (value) VALUES (42)");
            }
            
            // Query and verify
            try (Statement statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT * FROM test_table_2 WHERE value = 42")) {
                
                assertTrue(resultSet.next());
                assertEquals(42, resultSet.getInt("value"));
            }
            
            return null;
        });
    }
}
