package com.mlongbo.sunflower.commondb;

/**
 * 配置数据库
 * @author malongbo
 */
final public class Database {
    public static final Database me = new Database();

    private ConnectionProvider connectionProvider;

    public void setConnectionProvider(ConnectionProvider cp) {
        this.connectionProvider = cp;
    }

    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }
}
