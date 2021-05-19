package util;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.*;
import annotations.*;

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

    private ConnectionFactory(){
        super();
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
    public Connection getConnection(Object o) {

        Connection conn = null;
        Class<?> clazz = o.getClass();
        if(clazz.isAnnotationPresent(annotations.Connection.class)){
            try {
                conn = DriverManager.getConnection(
                        clazz.getAnnotation(annotations.Connection.class).url(),
                        clazz.getAnnotation(annotations.Connection.class).username(),
                        clazz.getAnnotation(annotations.Connection.class).password());

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return conn;

    }


}

