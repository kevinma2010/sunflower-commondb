package com.mlongbo.sunflower.commondb;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provide connection
 * @author malongbo
 */
public interface ConnectionProvider {
    /**
     *
     * @return a Connection object.
     * @throws java.sql.SQLException is an SQL error occured while retrieving the connection.
     */
    public Connection getConnection() throws SQLException;

    /**
     *
     */
    public void destroy();
}
