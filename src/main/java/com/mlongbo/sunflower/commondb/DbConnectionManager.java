package com.mlongbo.sunflower.commondb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库连接管理器
 * @author malongbo
 */
public final class DbConnectionManager {
    private static final Logger Log = LoggerFactory.getLogger(DbConnectionManager.class);
    private ConnectionProvider connectionProvider;
    private final Object providerLock = new Object();
    private static final ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();
    private final ThreadLocal<String> transaction = new ThreadLocal<String>();
    private static final DbConnectionManager instance = new DbConnectionManager();

    private DbConnectionManager(){};

    /**
     * 获取实例
     * @return
     */
    public static DbConnectionManager getInstance() {
       return instance;
    }

    /**
     * 释放资源
     */
    public void destroyConnectionProvider() {
        synchronized (providerLock) {
            if (connectionProvider != null) {
                connectionProvider.destroy();
                connectionProvider = null;
            }
        }
    }

    /**
     * 获取数据库连接
     * @return
     * @throws java.sql.SQLException
     */
    public Connection getThreadConnection() throws SQLException {
        Connection connection = threadLocal.get();
        if (connection != null)
            return connection;

        return getConnectionProvider().getConnection();
    }

    /**
     * 获取新连接
     * @return
     * @throws java.sql.SQLException
     */
    public Connection newConnection() throws SQLException {
        return getConnectionProvider().getConnection();
    }

    /**
     * 获取ConnectionProvider
     * @return
     */
    ConnectionProvider getConnectionProvider() {
        if (connectionProvider == null) {
            synchronized (providerLock) {
                if (connectionProvider == null) {
                    connectionProvider = Database.me.getConnectionProvider();
                }
            }
        }
        return connectionProvider;
    }

    /**
     * 开启数据库事务
     *
     * @return
     * @throws java.sql.SQLException
     */
    public Connection openTransactionConnection() throws SQLException {
        Connection connection = null;

        if (connection == null) {
            connection = getConnectionProvider().getConnection();
            threadLocal.set(connection);
        }

        try {
//            boolean autoCommit = true;
//            boolean autoCommit = connection.getAutoCommit();
//            if (autoCommit)
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 判断当前事务代理是否为一级代理
     * @param name
     * @return
     */
    public boolean isFirstTransact(String name) {
        String s = transaction.get();
        if (s == null) {
            transaction.set(name);
            return true;
        }

        return s.equals(name);
    }

    /**
     *
     * @return
     * @throws java.sql.SQLException
     */
    public Connection getTransactionConnection() throws SQLException {
        Connection connection = getThreadConnection();
        try {
            connection.setAutoCommit(false);
            return connection;
        } catch (SQLException e) {
            Log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 关闭事务连接
     * @param conn
     * @param abortTransaction
     */
    public void closeTransactionConnection(Connection conn, boolean abortTransaction) {
        if (conn != null) {

            try {
                if (abortTransaction) {
                    conn.rollback();
                } else {
                    conn.commit();
                }
            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
            }

            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
            }

        }

        close(conn);
    }

    /**
     * 关闭当前线程中的事务连接
     * 并将线程中的连接对象设置为null
     * @param abortTransaction
     */
    public void closeThreadTransactionConnection(boolean abortTransaction) {
        Connection connection = threadLocal.get();

        if (connection != null)
            closeTransactionConnection(connection, abortTransaction);

        threadLocal.set(null);
    }

    /**
     *
     * 关闭事务，同时关闭Statement
     * @param stat
     * @param conn
     * @param abortTransaction
     */
    public void closeTransactionConnection(Statement stat, Connection conn, boolean abortTransaction) {
        close(stat);
        closeTransactionConnection(conn, abortTransaction);
    }

    /**
     * 关闭连接
     * @param conn  Connection
     * @param stat   Statement
     * @param rs    ResultSet
     */
    public void close(Connection conn, Statement stat, ResultSet rs) {
        close(rs);
        close(stat);
        close(conn);
    }

    /**
     * 关闭数据库连接
     * @param conn  Connection
     */
    public void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭Statement
     * @param stat Statement
     */
    public void close(Statement stat) {
        if (stat != null) {
            try {
                stat.close();
            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭ResultSet
     * @param rs ResultSet
     */
    public void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                Log.error(e.getMessage(), e);
            }
        }
    }
}
