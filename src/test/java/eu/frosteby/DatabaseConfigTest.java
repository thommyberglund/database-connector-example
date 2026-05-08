package eu.frosteby;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseConfig class.
 */
public class DatabaseConfigTest {
    
    @TempDir
    Path tempDir;
    
    private Path originalDotenvPath;
    
    @BeforeEach
    void setUp() throws IOException {
        // Save original .env file path if it exists
        originalDotenvPath = Path.of(".env").toAbsolutePath();
        
        // Create a temporary .env file for testing
        Path testEnvPath = tempDir.resolve(".env");
        try (FileWriter writer = new FileWriter(testEnvPath.toFile())) {
            writer.write("DB_TYPE=mysql\n");
            writer.write("DB_HOST=test-host\n");
            writer.write("DB_PORT=3307\n");
            writer.write("DB_NAME=test-db\n");
            writer.write("DB_USER=test-user\n");
            writer.write("DB_PASSWORD=test-pass\n");
            writer.write("DB_CONNECTION_TIMEOUT=60\n");
        }
        
        // Set the dotenv file location for testing
        System.setProperty("dotenv.path", testEnvPath.toString());
    }
    
    @AfterEach
    void tearDown() {
        // Clear the dotenv path property
        System.clearProperty("dotenv.path");
    }
    
    @Test
    void testGetDbType_WithDefault() {
        // Remove DB_TYPE from test env
        System.setProperty("dotenv.path", tempDir.resolve("empty.env").toString());
        
        // Should return default (mysql)
        assertEquals("mysql", DatabaseConfig.getDbType());
    }
    
    @Test
    void testGetDbType_FromEnv() {
        assertEquals("mysql", DatabaseConfig.getDbType());
    }
    
    @Test
    void testGetHost_WithDefault() {
        System.setProperty("dotenv.path", tempDir.resolve("empty.env").toString());
        assertEquals("localhost", DatabaseConfig.getHost());
    }
    
    @Test
    void testGetHost_FromEnv() {
        assertEquals("test-host", DatabaseConfig.getHost());
    }
    
    @Test
    void testGetPort_WithDefaultMySQL() {
        System.setProperty("dotenv.path", tempDir.resolve("empty.env").toString());
        assertEquals(3306, DatabaseConfig.getPort());
    }
    
    @Test
    void testGetPort_WithDefaultPostgreSQL() throws IOException {
        Path postgresEnv = tempDir.resolve("postgres.env");
        try (FileWriter writer = new FileWriter(postgresEnv.toFile())) {
            writer.write("DB_TYPE=postgresql\n");
        }
        System.setProperty("dotenv.path", postgresEnv.toString());
        assertEquals(5432, DatabaseConfig.getPort());
    }
    
    @Test
    void testGetPort_FromEnv() {
        assertEquals(3307, DatabaseConfig.getPort());
    }
    
    @Test
    void testGetDatabaseName_FromEnv() {
        assertEquals("test-db", DatabaseConfig.getDatabaseName());
    }
    
    @Test
    void testGetDatabaseName_Missing() {
        System.setProperty("dotenv.path", tempDir.resolve("empty.env").toString());
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            DatabaseConfig::getDatabaseName);
        assertTrue(exception.getMessage().contains("DB_NAME"));
    }
    
    @Test
    void testGetUsername_FromEnv() {
        assertEquals("test-user", DatabaseConfig.getUsername());
    }
    
    @Test
    void testGetUsername_Missing() {
        System.setProperty("dotenv.path", tempDir.resolve("empty.env").toString());
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            DatabaseConfig::getUsername);
        assertTrue(exception.getMessage().contains("DB_USER"));
    }
    
    @Test
    void testGetPassword_FromEnv() {
        assertEquals("test-pass", DatabaseConfig.getPassword());
    }
    
    @Test
    void testGetPassword_Missing() {
        System.setProperty("dotenv.path", tempDir.resolve("empty.env").toString());
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            DatabaseConfig::getPassword);
        assertTrue(exception.getMessage().contains("DB_PASSWORD"));
    }
    
    @Test
    void testGetConnectionTimeout_WithDefault() {
        System.setProperty("dotenv.path", tempDir.resolve("empty.env").toString());
        assertEquals(30, DatabaseConfig.getConnectionTimeout());
    }
    
    @Test
    void testGetConnectionTimeout_FromEnv() {
        assertEquals(60, DatabaseConfig.getConnectionTimeout());
    }
    
    @Test
    void testGetJdbcUrl_MySQL() {
        String url = DatabaseConfig.getJdbcUrl();
        assertTrue(url.startsWith("jdbc:mysql://"));
        assertTrue(url.contains("test-host"));
        assertTrue(url.contains("3307"));
        assertTrue(url.contains("test-db"));
        assertTrue(url.contains("useSSL=false"));
    }
    
    @Test
    void testGetJdbcUrl_PostgreSQL() throws IOException {
        Path postgresEnv = tempDir.resolve("postgres.env");
        try (FileWriter writer = new FileWriter(postgresEnv.toFile())) {
            writer.write("DB_TYPE=postgresql\n");
            writer.write("DB_HOST=pg-host\n");
            writer.write("DB_PORT=5433\n");
            writer.write("DB_NAME=pg-db\n");
            writer.write("DB_USER=user\n");
            writer.write("DB_PASSWORD=pass\n");
        }
        System.setProperty("dotenv.path", postgresEnv.toString());
        
        String url = DatabaseConfig.getJdbcUrl();
        assertTrue(url.startsWith("jdbc:postgresql://"));
        assertTrue(url.contains("pg-host"));
        assertTrue(url.contains("5433"));
        assertTrue(url.contains("pg-db"));
    }
    
    @Test
    void testValidate_WithValidConfig() {
        // Should not throw exception
        assertDoesNotThrow(DatabaseConfig::validate);
    }
    
    @Test
    void testValidate_MissingDatabaseName() {
        System.setProperty("dotenv.path", tempDir.resolve("partial.env").toString());
        
        // Create partial env without DB_NAME
        Path partialEnv = tempDir.resolve("partial.env");
        try (FileWriter writer = new FileWriter(partialEnv.toFile())) {
            writer.write("DB_USER=user\n");
            writer.write("DB_PASSWORD=pass\n");
        }
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            DatabaseConfig::validate);
        assertTrue(exception.getMessage().contains("DB_NAME"));
    }
}
