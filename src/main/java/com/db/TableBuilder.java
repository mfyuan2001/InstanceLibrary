package com.db;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;

import javax.persistence.Table;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;


/**
 * @author yuanmengfan
 * @date 2022/7/9 16:06
 * @description
 */
public class TableBuilder {

    /**
     * //TODO 根据全类名生成建表语句
     * @title createTableSql
     * @param className
     * @param dbType
     * @return java.lang.String
     * @author yuanmengfan
     * @date 2022/7/12 22:36
     */
    public String createTableSql(String className, DbType dbType) throws ClassNotFoundException {
        if(StrUtil.isNotBlank(className)) throw new RuntimeException("className cannot null");

        return createTableSql(Class.forName(className),dbType);
    }

    /**
     * //TODO 根据Model对象生成建表语句
     *
     * @param model  需要生成表的Model对象
     * @param dbType 数据库的类型
     * @return java.lang.String
     * @title createTableSql
     * @author yuanmengfan
     * @date 2022/7/12 22:01
     */
    public String createTableSql(Class<?> model, DbType dbType) {
        String result = "";
        switch (dbType) {
            case MySQL:
                result = createMySQLTableSQL(model);
                break;
            case Oracle:
                result = createOracleTableSQL(model);
                break;
            default:
                break;
        }
        return result;
    }


    /**
     * //TODO 生成MySQL类型的建表语句
     *
     * @param model
     * @return java.lang.String
     * @title createMySQLTableSQL
     * @author yuanmengfan
     * @date 2022/7/12 22:03
     */
    private String createMySQLTableSQL(Class<?> model) {
        // model不能为空
        Objects.requireNonNull(model, "MODEL MUST NOT NULL");

        StringBuffer sql = new StringBuffer();

        TableExtension extension = model.getAnnotation(TableExtension.class);

        // 表注释
        String columnRemark = extension == null ? "" : extension.remark();
        // 表名
        String tableName = getTableName(model);
        // 主体内容
        String context = getMySQLModelContext(model);

        // 前置处理表存在的导致表创建不了的Sql
        sql.append("DROP TABLE IF EXISTS `").append(tableName).append("`;\n");

        // 构建CREATE TABLE 语句
        sql.append("CREATE TABLE ").append("`").append(tableName).append("`").append("(\n")
                .append(context)
                .append(") ").append(" COMMENT '").append(columnRemark).append("';");
        return sql.toString();
    }

    /**
     * //TODO 根据model对象中的扩张属性来处理，有哪些字段？是什么样类型？是什么样的约束条件？并生成对应的Sql
     *
     * @param model
     * @return java.lang.String
     * @title getMySQLModelContext
     * @author yuanmengfan
     * @date 2022/7/12 22:10
     */
    private String getMySQLModelContext(Class<?> model) {
        Field[] declaredFields = model.getDeclaredFields();
        StringBuffer context = new StringBuffer();
        Arrays.stream(declaredFields).forEach(field -> {
            // 根据对应的字段类型与数据库类型获取匹配的FieldToColumnType
            FieldToColumnType columnTypeByField = FieldToColumnType.getColumnTypeByField(field.getType().getName(), DbType.MySQL);

            // 字段名
            String fieldName = field.getName();
            // 数据库列类型
            String columnType = columnTypeByField.getColumnType();
            // 字段类型长度
            String columnLength = StrUtil.isBlank(columnTypeByField.getColumnLength()) ?
                    "" : String.format("(%s)", columnTypeByField.getColumnLength());
            // 字段是否为主键
            boolean idPresent = false;
            // 字段是否不能为NULL
            boolean isNotNull = false;
            // 字段默认值
            String defaultValue = "";
            // 数据库字段注释
            String columnRemark = "";

            // 判断该字段是否有扩展属性的这个注解 如果有个话 针对每个字段进行特殊处理
            TableExtension extension = field.getAnnotation(TableExtension.class);
            if (extension != null) {
                // 扩展属性的 isNotNull 为true 时defaultValue不能为 DefaultValues.NULL
                if (extension.isNotNull() && extension.defaultValue().equals(DefaultValues.NULL))
                    throw new RuntimeException("Table " + model.getSimpleName() + " Field " + fieldName +
                            "! When Extension IsNotNull are true, DefaultValue cannot be DefaultValues.NULL.");
                fieldName = StrUtil.isNotBlank(extension.columnName()) ? extension.columnName() : fieldName;
                columnLength = extension.length() == Integer.MIN_VALUE ?
                        columnLength : String.format("(%s)", extension.length());
                idPresent = extension.isId();
                isNotNull = extension.isNotNull();
                // 可自定义默认值
                defaultValue = getDefaultValue(extension.defaultValue());
                columnRemark = extension.remark();
            }
            context.append("\t")
                    .append(" `").append(fieldName).append("` ")
                    .append(columnType)
                    .append(columnLength)
                    // 该字段是主键的话添加 PRIMARY KEY这个关键字
                    .append(idPresent ? " PRIMARY KEY" : "")
                    // 该字段如果不为NULL添加 NOT NULL这个关键字
                    .append(isNotNull ? " NOT NULL" : "")
                    .append(StrUtil.isBlank(defaultValue) ? "" : (" DEFAULT " + defaultValue))
                    .append(" COMMENT '").append(columnRemark).append("'")
                    // 判断是否为最后一个字段 做最后一个逗号的处理
                    .append(field == declaredFields[declaredFields.length - 1] ? "" : ",")
                    .append("\n");
        });
        return context.toString();
    }
    /**
     * //TODO 生成Oracle类型的建表语句
     * @title createOracleTableSQL
     * @param model
     * @return java.lang.String
     * @author yuanmengfan
     * @date 2022/7/12 22:23
     */
    private String createOracleTableSQL(Class<?> model) {
        Objects.requireNonNull(model, "MODEL MUST NOT NULL");
        StringBuffer sql = new StringBuffer();
        // 表名
        String tableName = getTableName(model).toUpperCase();
        // 主体内容
        String context = getOracleModelContext(model);
        // 主体内容
        String fieldNodes = getOracleFieldNotes(model, tableName);

        // 表存在则删除表的的前置语句
        sql.append(String.format("DECLARE \n" +
                "      NUM   NUMBER; \n" +
                "BEGIN \n" +
                "      SELECT COUNT(1) INTO NUM FROM ALL_TABLES WHERE TABLE_NAME = '%1$s'; \n" +
                "      IF   NUM = 1   THEN \n" +
                "          EXECUTE IMMEDIATE 'DROP TABLE %1$s'; \n" +
                "      END IF; \n" +
                "END;\n", tableName));

        // 处理begin 后不能执行其他语句的问题
        sql.append("/\n");

        sql.append("CREATE TABLE ").append(tableName).append("(\n")
                .append(context)
                .append(") ;");
        sql.append("\n").append(fieldNodes);
        return sql.toString();
    }
    /**
     * //TODO 根据model对象中的扩张属性来处理，有哪些字段？是什么样类型？是什么样的约束条件？并生成对应的Sql
     * 由于Oracle跟MySQL的建表语句有一些细节的差异 所有封装的方法就分开写了 怕之后不好扩展
     * @param model
     * @return java.lang.String
     * @title getOracleModelContext
     * @author yuanmengfan
     * @date 2022/7/12 22:10
     */
    private String getOracleModelContext(Class<?> model) {
        String tableName = model.getSimpleName().toUpperCase();
        Field[] declaredFields = model.getDeclaredFields();
        StringBuffer context = new StringBuffer();
        Arrays.stream(declaredFields).forEach(field -> {
            FieldToColumnType columnTypeByField = FieldToColumnType.getColumnTypeByField(field.getType().getName(), DbType.Oracle);
            // 字段名
            String fieldName = field.getName();
            // 数据库列类型
            String columnType = columnTypeByField.getColumnType();
            // 字段类型长度
            String columnLength = StrUtil.isBlank(columnTypeByField.getColumnLength()) ?
                    "" : String.format("(%s)", columnTypeByField.getColumnLength());
            // 字段是否为主键
            boolean idPresent = false;
            // 字段是否不能为NULL
            boolean isNotNull = false;
            // 字段默认值
            String defaultValue = "";
            TableExtension extension = field.getAnnotation(TableExtension.class);
            if (extension != null) {
                if (extension.isNotNull() && extension.defaultValue().equals(DefaultValues.NULL))
                    throw new RuntimeException("Table " + tableName + " Field " + fieldName +
                            "! When Extension IsNotNull are true, DefaultValue cannot be DefaultValues.NULL.");

                fieldName = (StrUtil.isNotBlank(extension.columnName()) ? extension.columnName() : fieldName).toUpperCase();
                columnLength = extension.length() == Integer.MIN_VALUE ?
                        columnLength : String.format("(%s)", extension.length());
                idPresent = extension.isId();
                isNotNull = extension.isNotNull();
                defaultValue = getDefaultValue(extension.defaultValue());
            }
            context.append("\t")
                    .append(" ").append(fieldName).append(" ")
                    .append(columnType)
                    .append(columnLength)
                    .append(idPresent ? " PRIMARY KEY" : "")
                    .append(StrUtil.isBlank(defaultValue) ? "" : (" DEFAULT " + defaultValue))
                    .append(isNotNull ? " NOT NULL" : "")
                    // 判断是否为最后一个字段 做最后一个逗号的处理
                    .append(field == declaredFields[declaredFields.length - 1] ? "" : ",")
                    .append("\n");
        });
        return context.toString();
    }

    /**
     * //TODO 生成添加Oracle注释的语句
     * @title getOracleFieldNotes
     * @param model
     * @param tableName
     * @return java.lang.String
     * @author yuanmengfan
     * @date 2022/7/12 22:21
     */
    private String getOracleFieldNotes(Class<?> model, String tableName) {
        StringBuffer fieldNotes = new StringBuffer();
        TableExtension extension = model.getAnnotation(TableExtension.class);
        // 生成表注释
        if (extension != null && StrUtil.isNotBlank(extension.remark())) {
            fieldNotes.append(String.format("comment on table %s is '%s' ;\n", tableName, extension.remark()));
        }
        // 只为有remark值的字段添加注释
        Arrays.stream(model.getDeclaredFields()).filter(field -> {
            TableExtension fieldAnnotation = field.getAnnotation(TableExtension.class);
            if (fieldAnnotation != null && StrUtil.isNotBlank(fieldAnnotation.remark())) return true;
            else return false;
        }).forEach(field -> {
            TableExtension fieldAnnotation = field.getAnnotation(TableExtension.class);
            String fieldName = (StrUtil.isNotBlank(fieldAnnotation.columnName()) ? fieldAnnotation.columnName() : field.getName()).toUpperCase();
            fieldNotes.append(String.format("comment on column %s.%s is '%s';\n"
                    , tableName, fieldName, fieldAnnotation.remark()));
        });
        return fieldNotes.toString();
    }

    /**
     * //TODO 处理特殊的默认值
     * @title getDefaultValue
     * @param defaultValue
     * @return java.lang.String
     * @author yuanmengfan
     * @date 2022/7/12 22:20
     */
    private String getDefaultValue(String defaultValue) {
        if (StrUtil.isBlank(defaultValue)) return "";
        switch (defaultValue) {
            case DefaultValues.NULL:
            case DefaultValues.EMPTY_STRING:
            case DefaultValues.NULL_STRING:
            case DefaultValues.TRUE:
            case DefaultValues.FALSE:
                return defaultValue;
            default:
                return "'" + defaultValue + "'";
        }
    }

    /**
     * //TODO 根据Model类获取生成的表名
     *
     * @param model Model对象
     * @return java.lang.String
     * @title getTableName
     * @author yuanmengfan
     * @date 2022/7/12 21:59
     */
    private String getTableName(Class<?> model) {
        String tableName = "";
        // 当model 有 Table 这个注解时 且 Table注解的name值不是空时 直接取tableName为 生成表名
        // 否则 类名 当表名
        if (model.isAnnotationPresent(Table.class) && StrUtil.isNotBlank(model.getAnnotation(Table.class).name())) {
            tableName = model.getAnnotation(Table.class).name();
        } else {
            tableName = model.getSimpleName();
        }
        return tableName;
    }

    public static void main(String[] args) {
        System.out.println(new File("").getAbsolutePath());
        System.out.println(new TableBuilder().createTableSql(TestModel.class, DbType.MySQL));
    }
}
