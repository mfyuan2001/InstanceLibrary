package com.db;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.Session;
import cn.hutool.db.handler.BeanHandler;
import cn.hutool.db.handler.BeanListHandler;
import cn.hutool.db.handler.EntityHandler;
import cn.hutool.db.handler.EntityListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * @author yuanmengfan
 * @date 2022/7/21 21:16
 * @description
 */
public class DbContext {

    private static final Logger logger = LoggerFactory.getLogger(DbContext.class);


    Db db;
    Session session;

    SqlUtils sqlUtils = new SqlUtils();

    public int executeNoQuery(SqlEntity sqlEntity) throws SQLException {
        return db.execute(sqlEntity.getSql(), sqlEntity.getParams());
    }

    public Entity find(SqlEntity sqlEntity) throws SQLException {
        return db.query(sqlEntity.getSql(), new EntityHandler(), sqlEntity.getParams());
    }

    public List<Entity> findAll(SqlEntity sqlEntity) throws SQLException {
        return db.query(sqlEntity.getSql(), new EntityListHandler(), sqlEntity.getParams());
    }

    public <T> T find(SqlEntity sqlEntity, Class<T> clazz) throws SQLException {
        return db.query(sqlEntity.getSql(), new BeanHandler<>(clazz), sqlEntity.getParams());
    }

    public <T> List<T> findAll(SqlEntity sqlEntity, Class<T> clazz) throws SQLException {
        return db.query(sqlEntity.getSql(), new BeanListHandler<>(clazz), sqlEntity.getParams());
    }

    public  List<Entity> getTableMeta(String schema,String tableName) throws SQLException {
        return db.query("select `TABLE_SCHEMA`,`TABLE_NAME`,`TABLE_TYPE`,`ENGINE`,`TABLE_COMMENT` from `TABLES` where TABLE_TYPE = 'BASE TABLE' and TABLE_SCHEMA = ?   and TABLE_NAME like ?",schema,tableName);
    }

    public  List<Entity> getTableAll(String schema,String tableName) throws SQLException {
        return db.query("select `TABLE_SCHEMA`,`TABLE_NAME`,`TABLE_COMMENT` from `TABLES` where TABLE_TYPE = 'BASE TABLE' and TABLE_SCHEMA = ?   and TABLE_NAME like ? group by TABLE_SCHEMA,TABLE_NAME",schema,tableName);
    }



    public int add(Object... obj) throws SQLException {
        final int[] record = {0};
        if (ArrayUtil.isNotEmpty(obj)) {
            db.tx(parameter -> {
                for (Object model : obj) {
                    if (null != model) {
                        Entity entity = sqlUtils.generateEntity(model);
                        record[0] += parameter.insert(entity);
                    }
                }
            });
        }
        return record[0];
    }


    public int addExtensionList(Object obj) throws SQLException {
        final int[] record = {0};
        if (obj != null) {
            List<Entity> entityList = sqlUtils.generateEntityExtensionList(obj);
            db.tx(parameter -> {
                for (Entity entity : entityList) {
                    logger.info(entity.toString());
                    record[0] += parameter.insert(entity);
                }
            });
        }
        return record[0];
    }
}
