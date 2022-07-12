package com.db;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yuanmengfan
 * @date 2022/7/8 23:29
 * @description 枚举出想要对应的关系，从而使对应的Java类型匹配上对应的字段类型
 */

@Getter
public enum FieldToColumnType {
    // MySQL类型
    MYSQL_BYTE_COLUMN_TYPE("byte", "big", "1", DbType.MySQL),
    MYSQL_INT_COLUMN_TYPE("int", "int", "11", DbType.MySQL),
    MYSQL_SHORT_COLUMN_TYPE("short", "int", "6", DbType.MySQL),
    MYSQL_LONG_COLUMN_TYPE("long", "bigint", "20", DbType.MySQL),
    MYSQL_BOOLEAN_COLUMN_TYPE("boolean", "tinyint", "1", DbType.MySQL),
    MYSQL_FLOAT_COLUMN_TYPE("float", "float", "", DbType.MySQL),
    MYSQL_DOUBLE_COLUMN_TYPE("double", "double", "", DbType.MySQL),
    MYSQL_CHAR_COLUMN_TYPE("char", "VARCHAR", "50", DbType.MySQL),
    MYSQL_BYTE_PACKING_COLUMN_TYPE("com.lang.Byte", "big", "1", DbType.MySQL),
    MYSQL_INT_PACKING_COLUMN_TYPE("com.lang.Integer", "int", "11", DbType.MySQL),
    MYSQL_SHORT_PACKING_COLUMN_TYPE("com.lang.Short", "int", "6", DbType.MySQL),
    MYSQL_LONG_PACKING_COLUMN_TYPE("com.lang.Long", "bigint", "20", DbType.MySQL),
    MYSQL_BOOLEAN_PACKING_COLUMN_TYPE("com.lang.Boolean", "tinyint", "1", DbType.MySQL),
    MYSQL_FLOAT_PACKING_COLUMN_TYPE("com.lang.Float", "float", "", DbType.MySQL),
    MYSQL_DOUBLE_PACKING_COLUMN_TYPE("com.lang.Double", "double", "", DbType.MySQL),
    MYSQL_CHAR_PACKING_COLUMN_TYPE("com.lang.Character", "VARCHAR", "255", DbType.MySQL),
    MYSQL_BYTEARRAY_COLUMN_TYPE("byte[]", "blob", "", DbType.MySQL),
    MYSQL_DATE_COLUMN_TYPE("java.util.Date", "datetime", "", DbType.MySQL),
    MYSQL_SQL_DATE_COLUMN_TYPE("java.sql.Date", "data", "", DbType.MySQL),
    MYSQL_TIMESTAMP_COLUMN_TYPE("java.sql.TIMESTAMP", "timestamp", "", DbType.MySQL),
    DEFAULT_MYSQL_UNKNOWN_TYPE("String", "VARCHAR", "255", DbType.MySQL),

    // Oracle类型
    // Number类型是oralce的数值类型，存储的数值的精度可以达到38位。
    // Number是一种变长类型，长度为0-22字节。取值范围为:10^(-130) —— 10^126(不包括)。以十进制格式进行存储的，
    // 它便于存储，但是在计算上，系统会自动的将它转换成为二进制进行运算的。
    // Number(p,s)：
    // p和s都是可选的。
    // p指精度(precision)，即总位数。默认情况下精度为38。精度的取值范围为1~38。
    // s指小数位(scale)，小数点右边的位数。小数点位数的合法值为-84~127。小数位的默认值由精度来决定。如果没有指定精度，小数位默认为最大的取值区间。
    // 如果指定了精度，没有指定小数位。小数位默认为0(即没有小数位)。
    // 精度和小数位不会影响数据如何存储，只会影响允许哪些数值及数值如何舍入。
    ORACLE_BYTE_COLUMN_TYPE("byte", "CHAR", "1", DbType.Oracle),
    ORACLE_INT_COLUMN_TYPE("int", "NUMBER", "11", DbType.Oracle),
    ORACLE_SHORT_COLUMN_TYPE("short", "NUMBER", "6", DbType.Oracle),
    ORACLE_LONG_COLUMN_TYPE("long", "NUMBER", "20", DbType.Oracle),
    ORACLE_BOOLEAN_COLUMN_TYPE("boolean", "CHAR", "1", DbType.Oracle),
    ORACLE_FLOAT_COLUMN_TYPE("float", "NUMBER", "13,2", DbType.Oracle),
    ORACLE_DOUBLE_COLUMN_TYPE("double", "NUMBER", "23,2", DbType.Oracle),
    ORACLE_CHAR_COLUMN_TYPE("char", "VARCHAR2", "255", DbType.Oracle),
    ORACLE_BYTE_PACKING_COLUMN_TYPE("com.lang.Byte", "CHAR", "1", DbType.Oracle),
    ORACLE_INT_PACKING_COLUMN_TYPE("com.lang.Integer", "NUMBER", "11", DbType.Oracle),
    ORACLE_SHORT_PACKING_COLUMN_TYPE("com.lang.Short", "NUMBER", "6", DbType.Oracle),
    ORACLE_LONG_PACKING_COLUMN_TYPE("com.lang.Long", "NUMBER", "20", DbType.Oracle),
    ORACLE_BOOLEAN_PACKING_COLUMN_TYPE("com.lang.Boolean", "CHAR", "1", DbType.Oracle),
    ORACLE_FLOAT_PACKING_COLUMN_TYPE("com.lang.Float", "NUMBER", "13.2", DbType.Oracle),
    ORACLE_DOUBLE_PACKING_COLUMN_TYPE("com.lang.Double", "NUMBER", "23.2", DbType.Oracle),
    ORACLE_CHAR_PACKING_COLUMN_TYPE("com.lang.Character", "VARCHAR2", "255", DbType.Oracle),
    ORACLE_BYTEARRAY_COLUMN_TYPE("byte[]", "BLOB", "", DbType.Oracle),
    ORACLE_DATE_COLUMN_TYPE("java.util.Date", "DATE", "", DbType.Oracle),
    ORACLE_SQL_DATE_COLUMN_TYPE("java.sql.Date", "DATE", "", DbType.Oracle),
    ORACLE_TIMESTAMP_COLUMN_TYPE("java.sql.TIMESTAMP", "TIMESTAMP", "", DbType.MySQL),
    DEFAULT_ORACLE_UNKNOWN_TYPE("String", "VARCHAR2", "255", DbType.Oracle);

    // Model对象属性的类型
    private final String fieldType;
    // 数据库中列的类型 有些字段类型是可以没有长度的
    private final String columnType;
    // 数据库中列的长 或 精度
    private final String columnLength;
    // 数据库类型
    private final DbType dbType;

    FieldToColumnType(String fieldType, String columnType, String columnLength, DbType dbType) {
        this.fieldType = fieldType;
        this.columnType = columnType;
        this.columnLength = columnLength;
        this.dbType = dbType;
    }

    /**
     * //TODO
     *
     * @param fieldType Model属性类型 例如：int、java.util.Integer
     * @param dbType    数据库类型
     * @return com.db.FieldToColumnType
     * @title getColumnTypeByField 根据Model属性类型得到转换后数据中的类型
     * @author yuanmengfan
     * @date 2022/7/12 21:45
     */
    public static FieldToColumnType getColumnTypeByField(String fieldType, DbType dbType) {
        // fieldType 不能为空
        if (StrUtil.isBlank(fieldType)) throw new RuntimeException("fieldType is null");
        // dbType 不能为空
        Objects.requireNonNull(dbType, "DbType NOT NULL");
        // 寻找匹配的字段类型
        Optional<FieldToColumnType> first = getFieldToColumnTypesByDbType(dbType).stream()
                .filter((map) -> fieldType.equals(map.getFieldType())).findFirst();
        // 如果没有找到合适的类型直接返回该数据库类型中默认的类型
        if (first.isEmpty()) {
            return defaultTypeByDbType(dbType);
        }
        return first.get();
    }

    /**
     * 返回数据库中默认的字段类型
     *
     * @param dbType 数据库类型
     * @return com.db.FieldToColumnType
     * @title defaultTypeByDbType
     * @author yuanmengfan
     * @date 2022/7/12 21:51
     */
    private static FieldToColumnType defaultTypeByDbType(DbType dbType) {
        switch (dbType) {
            case MySQL:
                return FieldToColumnType.DEFAULT_MYSQL_UNKNOWN_TYPE;
            case Oracle:
                return FieldToColumnType.DEFAULT_ORACLE_UNKNOWN_TYPE;
            default:
                return null;
        }
    }

    /**
     * //TODO 返回为DbType的FieldToColumnType
     *
     * @param dbType
     * @return java.util.List<com.db.FieldToColumnType>
     * @title getFieldToColumnTypesByDbType
     * @author yuanmengfan
     * @date 2022/7/12 21:56
     */
    public static List<FieldToColumnType> getFieldToColumnTypesByDbType(DbType dbType) {
        return Arrays.stream(FieldToColumnType.values()).filter((map) -> dbType == map.getDbType()).collect(Collectors.toList());
    }
}
