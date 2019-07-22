import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.System.exit;

public class PostgresConnection {
    private Connection connection;
    private String databaseName;
    private Properties properties;

    public PostgresConnection(String databaseName) {
        this.databaseName = databaseName;
        this.properties = new Properties();
    }

    public Connection getConnection() {
        return connection;
    }

    public void tearDown() throws SQLException {
        connection.close();
    }

    public void connectToHost(String host, int port) throws SQLException {
        if (!checkIfCredentialsAreSet())
            exitWithMessage("FATAL: Credentials are not set. No connection to database could be established", 1);
        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, databaseName);
        this.connection = DriverManager.getConnection(url, properties);
    }

    public void setupCredentials(String username, String password) {
        properties.setProperty("user", username);
        properties.setProperty("password", password);
    }

    private boolean checkIfCredentialsAreSet() {
        return properties.getProperty("user") != null && properties.getProperty("password") != null;
    }

    private void exitWithMessage(String message, int exitCode) {
        System.err.println(message);
        exit(exitCode);
    }
}
