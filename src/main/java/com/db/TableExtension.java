package com.db;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yuanmengfan
 * @date 2022/7/9 16:59
 * @description 字段的扩展属性
 * /@Retention 的用法：指示注释类型的注释要保留多久。如果注释类型声明中不存在 Retention 注释，则保留策略默认为 RetentionPolicy.CLASS。
 * RetentionPolicy.CLASS 编译器将把注释记录在类文件中，但在运行时 VM 不需要保留注释
 * RetentionPolicy.RUNTIME 编译器将把注释记录在类文件中，在运行时 VM 将保留注释，因此可以反射性地读取
 * RetentionPolicy.SOURCE 编译器要丢弃的注释
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface TableExtension {
    // 是否为主键
    boolean isId() default false;

    // 自定义的列名
    String columnName() default "";

    // 字段的长度
    int length() default Integer.MIN_VALUE;

    // 字段是否不为空
    boolean isNotNull() default false;

    // 字段的默认值
    String defaultValue() default DefaultValues.NONE;

    // 字段的注释
    String remark() default "";
}
