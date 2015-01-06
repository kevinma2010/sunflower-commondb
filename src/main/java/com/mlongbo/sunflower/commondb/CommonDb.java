package com.mlongbo.sunflower.commondb;

import com.mlongbo.sunflower.commondb.util.StringUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库公共操作类库
 * @author malongbo
 */
public abstract class CommonDb<T> {
    private String tableName;
    private Object tableId = "id";
    private Class<T> clazz;
    private String defaultQueryFields = "*";
    private ThreadLocal<String> queryFields = new ThreadLocal<String>();

    /**
     * 默认的主键字段将使用"id"
     * @param tableName 映射的表名称,不能为空
     */
    public CommonDb(String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            throw new RuntimeException("tableName can not be blank");
        }
        this.tableName = tableName;
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.clazz = (Class<T>) type.getActualTypeArguments()[0];
    }

    /**
     * 设置表名称以及主键字段名称
     * @param tableName 映射的表名称,不能为空
     * @param tableId 映射的表主键
     */
    public CommonDb(String tableName, Object tableId) {
            this(tableName);
        this.tableId = tableId;
    }

    /**
     * 设置所有查询操作查询出的字段
     * @param fieldName 字段名与数据库中对应
     */
    public CommonDb<T> includeField(String... fieldName) {
        this.queryFields.set(StringUtils.join(fieldName, ","));
        return this;
    }

    /**
     * 重置查询字段，每次查询完成后会调用该函数
     * @return
     */
    private CommonDb<T> resetQueryFileds() {
        this.queryFields.set(null);
        return this;
    }

    /**
     * 设置默认查询字段，当调用queryFileds不存在时，将使用默认查询字段
     * @param defaultQueryField 字段名与数据库中对应
     */
    public CommonDb<T> setDefaultQueryFields(String... defaultQueryField) {
        this.defaultQueryFields = StringUtils.join(defaultQueryField, ",");
        return this;
    }

    public CommonDb(){}

    /**
     * 执行sql命令
     * @param sql sql字符串
     * @param params  预编译填充参数
     * @return  sql执行影响的行数
     * @throws java.sql.SQLException
     */
    public final Integer execute(String sql, Object... params) throws SQLException {
        return DbKit.execute(sql, params);
    }

    /**
     * 根据id删除记录
     * @param id
     * @return
     * @throws Exception
     */
    public final Boolean deleteById(Object id) throws Exception {
        //保证表名称字段与主键字段有效
        validateTableName().validatePrimaryKey();
        try {
            String sql = new StringBuffer().append("DELETE FROM `").append(this.tableName).append("` WHERE ")
                    .append(this.tableId).append("=?").toString();

            return execute(sql, id) > 0;
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /**
     * 自定条件删除记录
     * @param condition where语句后的sql
     * @param params 预编译填充参数
     * @return  sql执行影响的行数
     * @throws Exception
     */
    public final Integer deleteByCondition(String condition, Object... params) throws Exception {
        //保证表名称字段有效
        validateTableName();
        try {
            /*
            拼接sql语句
             */
            String sql = new StringBuffer("DELETE FROM `").append(this.tableName).append("`").toString();

            if (condition != null) {
               sql = new StringBuffer(sql).append(" WHERE ")
                       .append(condition).toString();
            }

            return execute(sql.toString(),params);
        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /**
     * count查询表
     * @return
     * @throws Exception
     */
    public final Integer count() throws Exception {
        return this.countByCondition(null);
    }

    /**
     * 自定义条件count查询表
     * @param condition  where语句后的sql
     * @param params  预编译填充参数
     * @return
     * @throws Exception
     */
    public final Integer countByCondition(String condition, Object... params) throws Exception {
        //保证表名称字段有效
        validateTableName();
        try {
            String sql = new StringBuffer("SELECT COUNT(*) `count` FROM ").append(this.tableName).toString();

            if (condition != null) {
                sql = new StringBuffer(sql).append(" WHERE ").append(condition).toString();
            }

            Object count = DbKit.query(new ScalarHandler("count"), sql, params);

            if (count instanceof Long) {
                Long countL = (Long)count;
                return countL.intValue();
            } else if (count instanceof Integer) {
                return (Integer)count;
            } else {
                return 0;
            }

        } catch (SQLException e) {
            throw new DbException(e);
        }
    }

    /**
     * 根据ID查询数据
     * @param id
     * @return
     * @throws Exception
     */
    public final T getById(Object id) throws Exception {
        //保证表名称字段与主键字段有效
        validateTableName().validatePrimaryKey();
        try {
            String sql = new StringBuffer("SELECT ").append(getQueryFields()).
                    append(" FROM `").append(this.tableName).append("` WHERE ")
                    .append(this.tableId).append("=?").toString();
            return DbKit.query(new BeanHandler<T>(this.clazz), sql, id);
        } catch (SQLException e) {
            throw new DbException(e);
        } finally {
            resetQueryFileds();
        }
    }

    /**
     * 根据条件查询第一条记录,建议使用getFirst函数
     * @param condition where语句后的sql
     * @param params  预编译填充参数
     * @return bean对象
     * @throws Exception
     */
    @Deprecated
    public final T getFirstByCondition(String condition, Object... params) throws Exception {
        //保证表名称字段有效
        validateTableName();
        try {
            String sql = new StringBuffer("SELECT ").append(getQueryFields()).
                    append(" FROM `").append(this.tableName).append("`").toString();

            if (condition != null) {
                sql = new StringBuffer(sql).append(" WHERE ")
                        .append(condition).toString();
            }

            return DbKit.query(new BeanHandler<T>(this.clazz), sql, params);
        } catch (SQLException e) {
            throw new DbException(e);
        }  finally {
            resetQueryFileds();
        }
    }

    /**
     * 根据条件查询第一条记录
     * @param condition where语句后的sql,请不要使用limit子句
     * @param params  预编译填充参数
     * @return bean对象
     * @throws Exception
     */
    public T findFirst(String condition, Object... params) throws Exception {
        //保证表名称字段有效
        validateTableName();
        StringBuffer buffer;
        try {
            buffer = new StringBuffer();
            buffer.append("SELECT ").append(getQueryFields()).
                    append(" FROM `").append(this.tableName).append("`").append(" WHERE ");

            if (condition != null) {
                buffer.append(condition);
            }

            buffer.append(" LIMIT 1");

            return DbKit.query(new BeanHandler<T>(this.clazz), buffer.toString(), params);
        } catch (SQLException e) {
            throw new DbException(e);
        }  finally {
            resetQueryFileds();
            buffer = null;
        }
    }

    /**
     * 根据id数组查询指定bean数据
     * @param id 可变参数，可指定多个id
     * @return  bean列表
     * @throws Exception
     */
    public List<T> getMulti(Object... id) throws Exception {
        if (id.length < 1) return new ArrayList<T>();

        String join = StringUtils.join(id, ",", "'");
        return fetchListByCondition(" "+tableId+" in ("+join+") ");
    }

    /**
     * 查询表数据列表
     * @return bean列表
     * @throws Exception
     */
    public final List<T> fetchList() throws Exception {
        //保证表名称字段有效
        validateTableName();
        try {
            String sql = new StringBuffer("SELECT ").append(getQueryFields()).
                    append(" FROM `").append(this.tableName).toString();
            return DbKit.query(new BeanListHandler<T>(this.clazz), sql);
        } catch (SQLException e) {
            throw new DbException(e);
        }  finally {
            resetQueryFileds();
        }
    }

    /**
     * 根据自定义条件查询列表
     * @param condition where语句后的sql
     * @param params 预编译填充参数
     * @return bean列表
     * @throws Exception
     */
    public final List<T> fetchListByCondition(String condition, Object... params) throws Exception {
        //保证表名称字段有效
        validateTableName();
        try {
            String sql = new StringBuffer("SELECT ").append(getQueryFields()).
                    append(" FROM `").append(this.tableName)
                    .append("`").toString();

            if (condition != null) {
                sql = new StringBuffer(sql).append(" WHERE ")
                        .append(condition).toString();
            }

            return DbKit.query(new BeanListHandler<T>(this.clazz), sql, params);
        } catch (SQLException e) {
            throw new DbException(e);
        }  finally {
            resetQueryFileds();
        }
    }

    /**
     * 分页查询
     * @param start 开始行数
     * @param count  要查询出的记录树，如10条记录
     * @return bean列表
     * @throws Exception
     */
    public final List<T> paginate(Integer start, Integer count) throws Exception {
        return paginate(null, start, count);
    }

    /**
     * 根据自定义条件分页查询
     * @param condition where语句后的sql
     * @param start 开始行数
     * @param count  要查询出的记录树，如10条记录
     * @param params 预编译填充参数
     * @return bean列表
     * @throws Exception
     */
    public final List<T> paginate(String condition, Integer start, Integer count, Object... params) throws Exception {
        //保证表名称字段有效
        validateTableName();
        try {
            String sql = new StringBuffer("SELECT ").append(getQueryFields()).
                    append(" FROM `").append(this.tableName)
                    .append("`").toString();

            if (condition != null) {
                sql = new StringBuffer(sql).append(" WHERE ")
                        .append(condition).toString();
            }

            sql = new StringBuffer(sql).append(" LIMIT ?,?").toString();

            /*
            合并参数
             */
            Object[] paramTmp = new Object[params.length + 2];
            System.arraycopy(params, 0, paramTmp, 0, params.length);
            paramTmp[params.length] = start;
            paramTmp[params.length+1] = count;


            return DbKit.query(new BeanListHandler<T>(this.clazz), sql, paramTmp);
        } catch (SQLException e) {
            throw new DbException(e);
        }  finally {
            resetQueryFileds();
        }
    }

    /**
     * 自定义查询
     * @param rsh 自定义实现的ResultSetHandler
     * @param sql  全sql命令
     * @param params 预编译填充参数
     * @return
     * @throws Exception
     */
    public final T query(ResultSetHandler<T> rsh,String sql, Object... params) throws Exception {
        try {
            return DbKit.query(rsh, sql, params);
        } catch (SQLException e) {
            throw new DbException(e);
        }  finally {
            resetQueryFileds();
        }
    }

    /**
     * 校验表名称是否有效
     * @return
     */
    private CommonDb<T> validateTableName() {
        if (this.tableName == null)
            throw new IllegalArgumentException("The field tableName can not be null!");
        return this;
    }

    /**
     * 校验主键字段是否有效
     * @return
     */
    private CommonDb<T> validatePrimaryKey() {
        if (this.tableId == null)
            throw new IllegalArgumentException("The primary key can not be null!");
        return this;
    }

    /**
     * 获取查询字段,优先使用includeField函数设置的值，否则使用setDefaultQueryFields函数设置的值 <br></br>，再否则就使用*了
     * @return
     */
    private String getQueryFields() {
        if (StringUtils.isNotEmpty(queryFields.get())) return queryFields.get();

        return defaultQueryFields;
    }
}
