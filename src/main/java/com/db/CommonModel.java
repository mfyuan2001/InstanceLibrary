package com.db;

import java.util.Date;

/**
 * @author yuanmengfan
 * @date 2022/7/17 16:26
 * @description 生成表结构的公共字段公共字段 public 修饰符的所有生成表中都会有 private 修饰符的只有子表会有
 */
public class CommonModel {
    @ColumnExtension(isId = true,length = 50)
    public String id;

    @ColumnExtension(length = 50)
    private String pid;

    @ColumnExtension(length = 50)
    private Integer index;

    public Date createDate;

    public Date updateDate;
}
