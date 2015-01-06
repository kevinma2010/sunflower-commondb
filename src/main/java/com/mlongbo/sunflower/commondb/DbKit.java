package com.mlongbo.sunflower.commondb;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库操作工具
 * @author malongbo
 */
public final class DbKit {
    private DbKit(){}
    private static DbConnectionManager connectionManager = DbConnectionManager.getInstance();
    private static QueryRunner runner = null;

    static {
        runner = new QueryRunner();
    }

    /**
     * 执行sql命令
     * @param sql sql字符串
     * @param params  预编译填充参数
     * @return  sql执行影响的行数
     * @throws java.sql.SQLException
     */
    public static int execute(String sql, Object... params) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionManager.getThreadConnection();
            return runner.update(connection, sql, params);
        } finally {
            if (connection.getAutoCommit())
                connectionManager.close(connection);
        }
    }

    /**
     * 自定义查询
     ** @param rsh 自定义实现的ResultSetHandler
     * @param sql  全sql命令
     * @param params 预编译填充参数
     * @param <T>
     * @return
     * @throws java.sql.SQLException
     */
    public static <T> T query(ResultSetHandler<T> rsh,String sql, Object... params) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionManager.newConnection();
            return runner.query(connection, sql, rsh, params);
        } finally {
            connectionManager.close(connection);
        }
    }

    /**
     * 执行批处理
     * @param sql  全sql命令
     * @param params 预编译填充参数
     * @return
     * @throws java.sql.SQLException
     */
    public static int[] batch(String sql, Object[][] params) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionManager.getThreadConnection();
            return runner.batch(connection, sql, params);
        } finally {
            if (connection.getAutoCommit())
                connectionManager.close(connection);
        }
    }
}
