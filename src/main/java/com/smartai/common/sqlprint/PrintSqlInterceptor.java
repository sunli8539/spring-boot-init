package com.smartai.common.sqlprint;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * type：标记需要拦截的类，只能是: StatementHandler | ParameterHandler | ResultSetHandler | Executor 类或者子类
 * method: 标记是拦截类的那个方法，method = "query", // 表示：拦截Executor的query方法
 * args: 标记拦截类方法的具体那个引用（尤其是重载时），query 有很多的重载方法，需要通过方法签名来指定具体拦截的是那个方法
 */
@Intercepts(value = {
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {
        MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class
    })
})
public class PrintSqlInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(PrintSqlInterceptor.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameterObject = null;
        if (invocation.getArgs().length > 1) {
            parameterObject = invocation.getArgs()[1];
        }
        long startTime = System.currentTimeMillis();
        //调用方法，实际上就是拦截的方法
        Object result = invocation.proceed();

        String statementId = mappedStatement.getId();
        if (statementId.endsWith("_mpCount")){
            return result;
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
        Configuration configuration = mappedStatement.getConfiguration();
        String sql = getSql(boundSql, parameterObject, configuration);
        long endTime = System.currentTimeMillis();
        long timing = endTime - startTime;
        if (log.isInfoEnabled()) {
            log.info("execute sql cost: " + timing + " ms" + " - id: " + statementId);
            log.info("Sql:  " + sql);
        }
        return result;
    }

    /**
     * 插入插件
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            //调用Plugin工具类，创建当前的类的代理类
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * 设置插件属性
     *
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {
    }

    /**
     * 拼接要打印的sql语句
     *
     * @param boundSql
     * @param parameterObject
     * @param configuration
     * @return
     */
    private String getSql(BoundSql boundSql, Object parameterObject, Configuration configuration) {
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        value = parameterObject;
                    } else {
                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
                        value = metaObject.getValue(propertyName);
                    }
                    sql = replacePlaceholder(sql, value);
                }
            }
        }
        return sql;
    }

    /**
     * 给sql语句中的条件加单引号，转换时间格式
     *
     * @param sql
     * @param propertyValue
     * @return
     */
    private String replacePlaceholder(String sql, Object propertyValue) {
        String result;
        if (propertyValue != null) {
            if (propertyValue instanceof String) {
                result = "'" + propertyValue + "'";
            } else if (propertyValue instanceof Date) {
                result = "'" + DATE_FORMAT.format(propertyValue) + "'";
            } else {
                result = propertyValue.toString();
            }
        } else {
            result = "null";
        }
        return sql.replaceFirst("\\?", Matcher.quoteReplacement(result));
    }
}
