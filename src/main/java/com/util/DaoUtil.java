package com.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.setting.Setting;

import javax.sql.DataSource;

/**
 * @author yuanmengfan
 * @date 2021/10/13 5:50 下午
 * @description
 */
public class DaoUtil {

    public static DataSource getDao(String daoName){
        Setting setting = new Setting("db/db.setting", CharsetUtil.CHARSET_UTF_8,true);
        //读取绝对路径文件/home/looly/XXX.setting（没有就创建，关于touc请查阅FileUtil）
        //第二个参数为自定义的编码，请保持与Setting文件的编码一致
        //第三个参数为是否使用变量，如果为true，则配置文件中的每个key都以被之后的条目中的value引用形式为 ${key}
        return DSFactory.create(setting).getDataSource(daoName);
    }
}
