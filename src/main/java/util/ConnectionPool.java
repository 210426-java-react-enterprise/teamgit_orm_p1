package util;

import com.mchange.v2.c3p0.*;

import java.beans.*;
import java.io.*;
import java.sql.*;
import java.util.*;

//ConnectionPools uses c3p0
public class ConnectionPool {

    private static ConnectionPool connectionPool;
    private static ComboPooledDataSource cpds = new ComboPooledDataSource();

    static {
        try {
            Properties props = new Properties();
            InputStream is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("application.properties");
            props.load(is);

            cpds.setDriverClass("org.postgresql.Driver");
            cpds.setJdbcUrl(props.getProperty("host-url"));
            cpds.setUser(props.getProperty("username"));
            cpds.setPassword(props.getProperty("password"));
        } catch (PropertyVetoException | IOException e) {
            e.printStackTrace();
        }
    }

    public ConnectionPool(){ }

    public static ConnectionPool getInstance() {
        if(connectionPool == null){
            connectionPool = new ConnectionPool();
        }
        return connectionPool;
    }

    public Connection getConnection() throws SQLException {
        return cpds.getConnection();
    }
}
