package com.db;

import cn.hutool.db.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author yuanmengfan
 * @date 2022/7/13 13:24
 * @description
 */
public class SqlUtils {

    private static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);


    public Entity CreateInsert(Object obj) {
        if (obj != null) {
            Entity entity;
            Class<?> modelClass = obj.getClass();
            List<Field> fields = getFields(modelClass);
            entity = Entity.create(TableBuilder.getTableName(modelClass));
            fields.stream()
                    .filter(field -> !field.getType().getTypeName().endsWith("List"))
                    .forEach(field -> {
                        try {
                            String fieldName = TableBuilder.getFieldName(field);
                            field.setAccessible(true);
                            Object value = field.get(obj);
                            if(value!=null){
                                entity.set(fieldName, value);
                            }
                        } catch (IllegalAccessException e) {
                            logger.error("赋值错误", e);
                        }
                    });
            return entity;
        }
        return null;
    }


    protected List<Field> getFields(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
    }
}
