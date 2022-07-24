package com.db;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author yuanmengfan
 * @date 2022/7/21 21:27
 * @description
 */
public class SqlEntity {

    private StringBuffer sql;
    private List<Object> params;

    public SqlEntity(String sql) {
        this.params = new ArrayList<>();
        this.sql = new StringBuffer(sql);
    }

    public static SqlEntity createSql(String sql) {
        return new SqlEntity(sql);
    }

    public SqlEntity addSql(String sql) {
        this.sql.append(" ").append(sql);
        return this;
    }

    public SqlEntity addParam(Object... param) {
        if (ArrayUtil.isNotEmpty(param)) {
            this.params.addAll(Arrays.asList(param));
        }
        return this;
    }

    public SqlEntity replaceParam(Object param, int index) {
        if (this.params.size() < index + 1) {
            throw new IndexOutOfBoundsException(String.format("Index %d out of bounds for length %d ", index, params.size()));
        }
        this.params.set(index, param);
        return this;
    }

    public Object[] getParams() {
        return this.params.toArray();
    }

    public String getSql() {
        return sql.toString();
    }

    @Override
    public String toString() {
        String result = sql.toString();
        if (params != null && !params.isEmpty()) {
            for (Object param : params) {
                if (param instanceof Date) {
                    result = result.replaceFirst("\\?", "'" + DateUtil.formatDate((Date) param) + "'");
                } else {
                    result = result.replaceFirst("\\?", "'" + param.toString() + "'");
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new ArrayList<>().toArray()));
        SqlEntity sqlEntity = SqlEntity.createSql("select `TABLE_SCHEMA`,`TABLE_NAME`,`TABLE_TYPE`,`ENGINE`,`TABLE_COMMENT` from `TABLES` where TABLE_TYPE = 'BASE TABLE'  and TABLE_SCHEMA = ? and TABLE_NAME like ?").addParam("sys","tables","1");
        System.out.println(sqlEntity);
    }
}
