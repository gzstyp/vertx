package com.fwtai.tool;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/** 连接数据库操作 */
public final class ToolSQLServer{

  // 用于操作数据库的客户端
  private final JDBCClient dbClient;

  public ToolSQLServer(final Vertx vertx){

    // 构造数据库的连接信息
    final JsonObject dbConfig = new JsonObject();
    dbConfig.put("url", "jdbc:sqlserver://192.168.4.244:1433;DatabaseName=gsomewsd2020820");
    dbConfig.put("driver_class","com.microsoft.sqlserver.jdbc.SQLServerDriver");
    dbConfig.put("user","sa");
    dbConfig.put("password","GZJZ@123");

    // 创建客户端
    dbClient = JDBCClient.createShared(vertx,dbConfig);
  }

  // 提供一个公共方法来获取客户端
  public JDBCClient getDbClient(){
    return dbClient;
  }

  public final void queryList(final String sql,final RoutingContext context,final JsonArray params){
    // 执行查询
    dbClient.queryWithParams(sql,params,res->{
      dbClient.close();
      if(res.succeeded()){
        final ResultSet resultSet = res.result();
        final List<JsonObject> rows = resultSet.getRows();
        final String json = ToolClient.queryJson(rows);
        ToolClient.responseJson(context,json);
      } else {
        ToolClient.responseJson(context,ToolClient.jsonFailure());
      }
    });
  }

  public final void queryMap(final String sql,final RoutingContext context,final JsonArray params){
    // 执行查询
    dbClient.queryWithParams(sql,params,res->{
      dbClient.close();
      if(res.succeeded()){
        final ResultSet resultSet = res.result();
        final List<JsonObject> rows = resultSet.getRows();
        if(rows.size() > 0){
          final JsonObject object = rows.get(0);
          final String json = ToolClient.queryJson(object);
          ToolClient.responseJson(context,json);
        }else{
          ToolClient.responseJson(context,ToolClient.jsonEmpty());
        }
      } else {
        ToolClient.responseJson(context,ToolClient.jsonFailure());
      }
    });
  }

  private void queryHashMap(final String sql,final RoutingContext context,final JsonArray params){
    // 执行查询
    dbClient.queryWithParams(sql,params,res->{
      dbClient.close();
      if(res.succeeded()){
        final ResultSet resultSet = res.result();
        final List<String> columnNames = resultSet.getColumnNames();
        final List<JsonArray> list = resultSet.getResults();
        if(list.size() > 0){
          final JsonArray jsonArray = list.get(0);
          final JsonObject jsonObject = new JsonObject();
          for(int i = 0; i < columnNames.size(); i++){
            final String key = columnNames.get(i);
            final Object value = jsonArray.getValue(i);
            jsonObject.put(key,value);
          }
          final String json = ToolClient.queryJson(jsonObject);
          ToolClient.responseJson(context,json);
        }else{
          ToolClient.responseJson(context,ToolClient.jsonEmpty());
        }
      } else {
        ToolClient.responseJson(context,ToolClient.jsonFailure());
      }
    });
  }
}