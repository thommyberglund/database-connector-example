package eu.frosteby;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatabaseConnector with MySQL Testcontainers.
 */
@Testcontainers
public class DatabaseConnectorMySqlTest {
    
    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(TestDatabaseConfig.MYSQL_IMAGE)
            .withDatabaseName(TestDatabaseConfig.MYSQL_DATABASE)
            .withUsername(TestDatabaseConfig.MYSQL_USERNAME)
            .withPassword(TestDatabaseConfig.MYSQL_PASSWORD);
    
    @Test
    void testMySqlConnection() throws SQLException {
        DatabaseConnector connector = new DatabaseConnector();
        
        try {
            // Connect using test container details
            String jdbcUrl = TestDatabaseConfig.getMySqlJdbcUrl(
                mysqlContainer.getHost(), 
                mysqlContainer.getFirstMappedPort()
            );
            
            Connection connection = DriverManager.getConnection(
                jdbcUrl,
                TestDatabaseConfig.MYSQL_USERNAME,
                TestDatabaseConfig.MYSQL_PASSWORD
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
    void testMySqlQueryExecution() throws SQLException {
        String jdbcUrl = TestDatabaseConfig.getMySqlJdbcUrl(
            mysqlContainer.getHost(), 
            mysqlContainer.getFirstMappedPort()
        );
        
        try (Connection connection = DriverManager.getConnection(
                jdbcUrl,
                TestDatabaseConfig.MYSQL_USERNAME,
                TestDatabaseConfig.MYSQL_PASSWORD)) {
            
            // Create a test table
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(100))");
                statement.execute("INSERT INTO test_table (id, name) VALUES (1, 'Test Value')");
            }
            
            // Query the test table
            try (Statement statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT * FROM test_table WHERE id = 1")) {
                
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt("id"));
                assertEquals("Test Value", resultSet.getString("name"));
            }
        }
    }
}
