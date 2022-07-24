package com.db.build.sql.mysql;


import cn.hutool.db.Entity;
import com.db.TableExtension;
import com.db.TestModel;
import com.db.build.field.BaseFieldBuilder;
import com.db.build.field.ExtensionFieldBuilder;
import com.db.build.field.FieldBuilder;
import com.db.build.sql.ContextBuilder;
import com.db.build.sql.SqlBuilder;
import com.db.util.FieldUtils;
import com.db.util.ModelToTableUtils;
import com.util.GenericsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yuanmengfan
 * @date 2022/7/23 15:00
 * @description
 */
public class MySQLSqlBuild implements SqlBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MySQLSqlBuild.class);

    private ContextBuilder contextBuilder = new MySQLContextBuilder();
    private FieldBuilder fieldBuilder;

    public MySQLSqlBuild(FieldBuilder fieldBuilder) {
        this.fieldBuilder = fieldBuilder;
    }

    public MySQLSqlBuild() {
        this.fieldBuilder = new BaseFieldBuilder();
    }

    @Override
    public String buildTableSql(Class<?> clazz) {
        return buildTableSql(clazz, true);
    }

    public String buildTableSql(Class<?> clazz, boolean isSuper) {
        // model不能为空
        Objects.requireNonNull(clazz, "MODEL MUST NOT NULL");

        StringBuffer sql = new StringBuffer();

        TableExtension extension = clazz.getAnnotation(TableExtension.class);

        // 表注释
        String columnRemark = extension == null ? "" : extension.remark();
        // 表名
        String tableName = ModelToTableUtils.getTableName(clazz);
        // 主体内容
        String context = contextBuilder.context(fieldBuilder.getField(clazz, isSuper));

        // 生成建表的声明语句
        sql.append("-- START CREATE TABLE ").append(tableName).append("\n");

        // 前置处理表存在的导致表创建不了的Sql
        sql.append("DROP TABLE IF EXISTS `").append(tableName).append("`;\n");

        // 构建CREATE TABLE 语句
        sql.append("CREATE TABLE ").append("`").append(tableName).append("`").append("(\n")
                .append(context)
                .append(") ").append(" COMMENT '").append(columnRemark).append("';").append("\n");

        // 生成建表的声明语句
        sql.append("-- END CREATE TABLE ").append(tableName).append("\n\n");

        // 拿到List类型的字段利用递归，生成子表
        FieldUtils.getListTypeFields(clazz).forEach(field -> {
            // 拿到这些字段的类型的第一个泛型类型 根据这个泛型的类型生成对应的子表
            try {
                sql.append(buildTableSql(Class.forName(GenericsUtil.getGenericsTypeNameByFiledAndIndex(field, 0)), false));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        return sql.toString();
    }

    /**
     * 连同子表一起生成Entity对象的集合
     *
     * @param obj 需要生成的对象
     * @return java.util.List<cn.hutool.db.Entity>
     * @title createInsertList
     * @author yuanmengfan
     * @date 2022/7/20 14:11
     */
    public List<Entity> createInsertList(Object obj) {
        if (obj != null) {
            // 生成主表Entity
            List<Entity> result = new ArrayList<>();
            Entity entity = createInsert(obj);
            result.add(entity);

            logger.info(entity.toString());

            // 拿到是List类型的字段 生成子表Entity
            FieldUtils.getListTypeFields(obj.getClass()).forEach(field -> {
                // 开放访问
                field.setAccessible(true);
                try {
                    // 利用迭代完成可重复判断子类
                    List<?> values = (List<?>) field.get(obj);
                    for (int i = 0; i < values.size(); i++) {
                        // 子类都带上父类的id
                        result.addAll(createInsertList(values.get(i)));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            return result;
        }
        return null;
    }

    /**
     * 生成Entity对象
     *
     * @param obj 需要生成的对象
     * @return cn.hutool.db.Entity
     * @title CreateInsert
     * @author yuanmengfan
     * @date 2022/7/20 14:05
     */
    public Entity createInsert(Object obj) {
        if (obj != null) {
            // 根据obj生成一个具有对应表名的Entity对象
            Class<?> modelClass = obj.getClass();
            Entity entity = Entity.create(ModelToTableUtils.getTableName(modelClass));

            // 拿到所需的字段
            List<Field> fields = fieldBuilder.getField(modelClass, true);

            fields.forEach(field -> setFieldValue(entity, field, obj));
            return entity;
        }
        return null;
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

    public static void main(String[] args) {
        System.out.println(new MySQLSqlBuild(new ExtensionFieldBuilder()).buildTableSql(TestModel.class));
    }
}