package util;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**Establishes the connection to the SQL server
 *
 */
public class ConnectionFactory {

    /**
     * Creates a connection to the postgresql server
     */
    private static ConnectionFactory connectionFactory;
    private Properties props = new Properties();

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Something went wrong!");
        }
    }

    /**
     * Directs the connection factory to separate file for the server log in credentials and reads them in
     */
    private ConnectionFactory() {
        try {
            props.load(new FileReader("src/main/resources/application.properties"));
        } catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }

    /**
     * checks if we have an instance of connection factory already running
     * @return
     */
    public static ConnectionFactory getInstance() {
        if (connectionFactory == null) {
            connectionFactory = new ConnectionFactory();
        }

        return connectionFactory;
    }

    /**
     * Uses the scraped server login and plugs it in to form a connection with the DB
     * @return
     */
    public Connection getConnection() {

        Connection conn = null;

        try {

            conn = DriverManager.getConnection(
                    props.getProperty("host-url"),
                    props.getProperty("username"),
                    props.getProperty("password"));

        } catch (SQLException e) {
            System.out.println("Something went wrong!");
        }

        return conn;

    }

}
