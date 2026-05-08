# Database Connector Example (Java 25 + Maven)

A skeleton Java 25 application using Maven for dependency management, demonstrating JDBC database connectivity with environment variable configuration via dotenv-java.

## 🏗 Project Structure

```
database-connector-example/
├── pom.xml                    # Maven configuration
├── .env.example               # Environment variables template
├── .gitignore                 # Git ignore rules
└── src/
    ├── main/java/eu/frosteby/
    │   ├── DatabaseConfig.java # Environment variable utility
    │   ├── DatabaseConnector.java # JDBC connection manager
    │   └── Main.java            # Demo application
    └── test/java/eu/frosteby/
        └── DatabaseConfigTest.java # Unit tests
```

## 📋 Prerequisites

- Java 25 JDK
- Maven 3.9+
- MySQL or PostgreSQL database (for testing)

## 🚀 Quick Start

### 1. Clone and Setup

```bash
git clone https://github.com/thommyberglund/database-connector-example.git
cd database-connector-example
```

### 2. Configure Environment

Copy the example environment file and update with your database credentials:

```bash
cp .env.example .env
```

Edit `.env` with your database configuration:

```env
DB_TYPE=mysql
DB_HOST=localhost
DB_PORT=3306
DB_NAME=your_database
DB_USER=your_username
DB_PASSWORD=your_password
DB_CONNECTION_TIMEOUT=30
```

### 3. Build and Run

```bash
# Build the project
mvn clean compile

# Run the main class
mvn exec:java -Dexec.mainClass="com.example.database.Main"
```

Or run directly:

```bash
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.example.database.Main
```

## 📦 Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| MySQL Connector/J | 8.3.0 | MySQL JDBC driver |
| PostgreSQL JDBC | 42.7.3 | PostgreSQL JDBC driver |
| dotenv-java | 3.0.5 | Environment variable loading |
| JUnit 5 | 5.10.0 | Unit testing |
| Testcontainers | 1.19.7 | Integration testing with Docker containers |
| Testcontainers MySQL | 1.19.7 | MySQL container support |
| Testcontainers PostgreSQL | 1.19.7 | PostgreSQL container support |

## 🔧 Configuration

### Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_TYPE` | No | `mysql` | Database type: `mysql` or `postgresql` |
| `DB_HOST` | No | `localhost` | Database host |
| `DB_PORT` | No | 3306 (MySQL) / 5432 (PostgreSQL) | Database port |
| `DB_NAME` | Yes | - | Database name |
| `DB_USER` | Yes | - | Database username |
| `DB_PASSWORD` | Yes | - | Database password |
| `DB_CONNECTION_TIMEOUT` | No | 30 | Connection timeout in seconds |

### JDBC Connection URLs

The connector automatically generates the appropriate JDBC URL based on `DB_TYPE`:

- **MySQL**: `jdbc:mysql://{host}:{port}/{database}?useSSL=false&serverTimezone=UTC`
- **PostgreSQL**: `jdbc:postgresql://{host}:{port}/{database}`

## 🔌 Usage

### Basic Connection

```java
import eu.frosteby.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;

public class Example {
    public static void main(String[] args) {
        DatabaseConnector connector = new DatabaseConnector();
        
        try {
            connector.connect();
            Connection connection = connector.getConnection();
            
            // Use the connection...
            
            connector.disconnect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

### Using the Callback Pattern

```java
import eu.frosteby.DatabaseConnector;
import eu.frosteby.DatabaseConnector.ConnectionCallback;
import java.sql.Connection;
import java.sql.SQLException;

public class Example {
    public static void main(String[] args) {
        try {
            DatabaseConnector.withConnection(connection -> {
                // Use the connection here
                // It will be automatically closed after this block
                return null;
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

### Accessing Configuration Directly

```java
import eu.frosteby.DatabaseConfig;

public class Example {
    public static void main(String[] args) {
        String jdbcUrl = DatabaseConfig.getJdbcUrl();
        String username = DatabaseConfig.getUsername();
        String password = DatabaseConfig.getPassword();
        
        System.out.println("Connecting to: " + jdbcUrl);
    }
}
```

## 🧪 Testing

Run all tests (unit + integration):

```bash
mvn test
```

Run only unit tests (skip integration tests):

```bash
mvn test -DskipITs=false
```

Run only integration tests:

```bash
mvn verify -DskipTests=true -DskipITs=false
```

### Test Types

| Test Class | Type | Description |
|------------|------|-------------|
| `DatabaseConfigTest` | Unit | Tests environment variable loading and configuration |
| `DatabaseConnectorMySqlTest` | Integration | Tests MySQL connection using Testcontainers |
| `DatabaseConnectorPostgreSqlTest` | Integration | Tests PostgreSQL connection using Testcontainers |

### Testcontainers

The project uses [Testcontainers](https://www.testcontainers.org/) for integration testing with real database containers:

- **MySQL**: `mysql:8.3` image
- **PostgreSQL**: `postgres:16` image

**Prerequisites for integration tests:**
- Docker installed and running
- Sufficient memory allocated to Docker (recommended: 4GB+)

**Disabling integration tests:**

To skip integration tests (useful when Docker is not available):

```bash
mvn test -DskipITs=true
```

Or use the Maven profile:

```bash
mvn test -Pno-integration-tests
```

Tests cover:
- Environment variable loading with defaults
- JDBC URL generation for MySQL and PostgreSQL
- Configuration validation
- Error handling for missing required variables
- Real database connection testing with MySQL
- Real database connection testing with PostgreSQL
- Query execution and result verification

## 📝 API Documentation

### DatabaseConfig

Static utility class for loading database configuration from environment variables.

| Method | Description |
|--------|-------------|
| `getDbType()` | Gets database type (mysql/postgresql) |
| `getHost()` | Gets database host |
| `getPort()` | Gets database port |
| `getDatabaseName()` | Gets database name (required) |
| `getUsername()` | Gets database username (required) |
| `getPassword()` | Gets database password (required) |
| `getConnectionTimeout()` | Gets connection timeout in seconds |
| `getJdbcUrl()` | Gets full JDBC connection URL |
| `validate()` | Validates all required variables are set |

### DatabaseConnector

Manages JDBC connections with automatic resource management.

| Method | Description |
|--------|-------------|
| `connect()` | Establishes a database connection |
| `disconnect()` | Closes the connection |
| `isConnected()` | Checks if connection is active |
| `getConnection()` | Gets the current Connection object |
| `testConnection()` | Tests the connection with a simple query |
| `withConnection(ConnectionCallback<T>)` | Static helper for automatic connection management |

## 🎯 Features

- ✅ Java 25 compatible
- ✅ Maven dependency management
- ✅ Environment variable configuration via dotenv-java
- ✅ Support for MySQL and PostgreSQL
- ✅ Connection pooling ready (add HikariCP or similar)
- ✅ Comprehensive error handling
- ✅ Unit tests with JUnit 5
- ✅ Callback pattern for automatic resource management
- ✅ Default values with fallback configuration

## 🔒 Security Notes

- **Never commit `.env` files** to version control (already in `.gitignore`)
- **Use strong passwords** for database credentials
- **Restrict database user permissions** to only what's needed
- **Use SSL/TLS** for production database connections
- **Consider using a secrets manager** for production deployments

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

## 📄 License

This project is open source and available under the MIT License.
