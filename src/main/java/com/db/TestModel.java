package com.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import javax.persistence.Table;
import java.util.Date;

/**
 * @author yuanmengfan
 * @date 2022/7/9 17:26
 * @description
 */

@Getter
@Setter
@Table(name = "t_testModel")
@TableExtension(remark = "测试表")
public class TestModel {
    @TableExtension(isId = true)
    private String id;
    @TableExtension(columnName = "userName",defaultValue = "name1", remark = "姓名")
    private String name;
    @TableExtension(remark = "年龄", length = 10)
    private int age;
    @TableExtension(defaultValue = DefaultValues.NULL, remark = "工资")
    private Double salary;
    @TableExtension(remark = "生日")
    private Date birth;
    @TableExtension(defaultValue=DefaultValues.TRUE,remark = "是否禁用")
    private boolean disabled;
}
