package com.db;

import cn.hutool.db.Entity;
import com.db.build.field.ExtensionFieldBuilder;
import com.db.util.FieldUtils;
import com.db.util.ModelToTableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author yuanmengfan
 * @date 2022/7/13 13:24
 * @description 生成与数据库操作的对象
 */
public class SqlUtils {

    private static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);

    /**
     * 生成Entity对象
     *
     * @param obj 需要生成的对象
     * @return cn.hutool.db.Entity
     * @title generateEntity
     * @author yuanmengfan
     * @date 2022/7/20 14:05
     */
    public Entity generateEntity(Object obj) {
        if (obj != null) {
            // 根据obj生成一个具有对应表名的Entity对象
            Class<?> modelClass = obj.getClass();
            Entity entity = Entity.create(ModelToTableUtils.getTableName(modelClass));

            // 拿到所需的字段
            List<Field> fields = FieldUtils.getBaseField(obj.getClass());
            fields.forEach(field -> setFieldValue(entity, field, obj));

            return entity;
        }
        return null;
    }

    /**
     * 生成主表与子表Entity集合
     * @title generateEntityExtensionList
     * @param obj
     * @return java.util.List<cn.hutool.db.Entity>
     * @author yuanmengfan
     * @date 2022/7/24 16:33
     */
    public List<Entity> generateEntityExtensionList(Object obj) {
        return generateEntityExtensionList(obj, true, null, null);
    }

    private List<Entity> generateEntityExtensionList(Object obj, boolean isSuper, String pid, Integer index) {
        if (obj != null) {
            List<Entity> result = new ArrayList<>();
            Entity entity = generateEntityExtension(obj, isSuper, pid, index);
            result.add(entity);

            // 拿到是List类型的字段 生成子表Entity
            FieldUtils.getListTypeFields(obj.getClass()).forEach(field -> {
                // 开放访问
                field.setAccessible(true);
                try {
                    // 利用迭代完成可重复判断子类
                    List<?> values = (List<?>) field.get(obj);
                    for (int i = 0; i < values.size(); i++) {
                        // 子类都带上父类的id
                        result.addAll(generateEntityExtensionList(values.get(i), false, entity.getStr("id"), i + 1));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            return result;
        }
        return null;
    }

    private Entity generateEntityExtension(Object obj, boolean isSuper, String pid, Integer index) {
        ExtensionFieldBuilder extensionFieldBuilder = new ExtensionFieldBuilder();

        // 根据obj生成一个具有对应表名的Entity对象
        Class<?> modelClass = obj.getClass();
        Entity entity = Entity.create(ModelToTableUtils.getTableName(modelClass));

        // 拿到所需的字段
        List<Field> fields = extensionFieldBuilder.getField(modelClass, isSuper);

        fields.forEach(field -> {
            //判断该字段是否来自公共类 如果来着公共类由这里生成对应的值
            if (field.getDeclaringClass() == CommonModel.class) {
                switch (field.getName()) {
                    case "id":
                        entity.set(field.getName(), UUID.randomUUID().toString());
                        break;
                    case "pid":
                        entity.set(field.getName(), pid);
                        break;
                    case "index":
                        entity.set(field.getName(), index);
                        break;
                    case "createDate":
                    case "updateDate":
                        entity.set(field.getName(), LocalDateTime.now());
                        break;
                }
            } else {
                setFieldValue(entity, field, obj);
            }
        });
        return entity;


    }

    private void setFieldValue(Entity entity, Field field, Object obj) {
        try {
            // 开放访问
            field.setAccessible(true);
            // 获取字段名
            String fieldName = ModelToTableUtils.getFieldName(field);
            // 获取该字段的值
            Object value = field.get(obj);
            // 值为空的字段不添加
            if (value != null) {
                entity.set(fieldName, value);
            }
        } catch (IllegalAccessException e) {
            logger.error("赋值错误", e);
        }
    }
}
