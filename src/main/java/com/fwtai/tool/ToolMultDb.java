package com.fwtai.tool;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

/**
 * 多源数据库
 * https://www.cnblogs.com/endv/p/11247947.html
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-12-29 0:07
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
 */
public final class ToolMultDb{

  //用于操作数据库的客户端
  private JDBCClient dbClient;

  public ToolMultDb(final Vertx vertx){
    // 构造数据库的连接信息
    final JsonObject dbConfig = new JsonObject();
    dbConfig.put("url","jdbc:mysql://192.168.3.66:3306/cdc_backend?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowMultiQueries=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
    dbConfig.put("driver_class","com.mysql.cj.jdbc.Driver");
    dbConfig.put("user","root");
    dbConfig.put("password","rootFwtai");
    dbClient = JDBCClient.createShared(vertx, dbConfig);
  }

  //提供一个公共方法来获取客户端
  public JDBCClient getDbClient(){
    return dbClient;
  }
}