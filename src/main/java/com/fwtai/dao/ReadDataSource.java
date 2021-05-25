package com.fwtai.dao;

import com.fwtai.callback.QueryResultList;
import com.fwtai.callback.QueryResultMap;
import com.fwtai.config.ConfigFiles;
import com.fwtai.tool.ToolClient;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 读库数据源
 * @用法 final ReadDataSource readClient = new ReadDataSource(vertx);
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-04 9:02
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class ReadDataSource{

  final InternalLogger logger = Log4JLoggerFactory.getInstance(getClass());

  private final AtomicInteger count = new AtomicInteger(0);

  private final int dbTotal = 2;//两个读库

  private final DBRead0 dbRead0;
  private final DBRead1 dbRead1;

  public ReadDataSource(final Vertx vertx){
    this.dbRead0 = new DBRead0(vertx);
    this.dbRead1 = new DBRead1(vertx);
  }

  public MySQLPool getPool(){
    final int total = count.getAndAdd(1);
    final int key = total % dbTotal;
    if(key == 0){
      return this.dbRead0.getClient();
    }else{
      return this.dbRead1.getClient();
    }
  }

  //不推荐无参数 new ToolMySQL(vertx).queryList();
  private final void queryList(final RoutingContext context,final String sql){
    getPool().getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final ArrayList<JsonObject> list = new ArrayList<>();
            final RowSet<Row> rowSet = rows.result();
            final List<String> columns = rowSet.columnsNames();
            rowSet.forEach((item) ->{
              final JsonObject jsonObject = new JsonObject();
              for(int i = 0; i < columns.size(); i++){
                final String column = columns.get(i);
                jsonObject.put(column,item.getValue(column));
              }
              list.add(jsonObject);
            });
            //操作数据库成功
            ToolClient.responseJson(context,ToolClient.queryJson(list));
          }else{
            logger.error("queryList()出现异常,连接数据库失败:"+sql);
            //操作数据库失败
            final String json = ToolClient.createJson(199,"连接数据库失败");
            ToolClient.responseJson(context,json);
          }
        });
      }
    });
  }

  //todo 推荐,有参数 new ToolMySQL(vertx).queryList();若没有参数的话,要创建 new ArrayList<Object>(1) 作为第3个参数
  public final void queryList(final RoutingContext context,final String sql,final List<Object> params){
    getPool().getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(Tuple.wrap(params),rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final ArrayList<JsonObject> list = new ArrayList<>();
            final RowSet<Row> rowSet = rows.result();
            final List<String> columns = rowSet.columnsNames();
            rowSet.forEach((item) ->{
              final JsonObject jsonObject = new JsonObject();
              for(int i = 0; i < columns.size(); i++){
                final String column = columns.get(i);
                jsonObject.put(column,item.getValue(column));
              }
              list.add(jsonObject);
            });
            //操作数据库成功
            ToolClient.responseJson(context,ToolClient.queryJson(list));
          }else{
            logger.error("queryList()出现异常,连接数据库失败:"+sql);
            //操作数据库失败
            final String json = ToolClient.createJson(199,"连接数据库失败");
            ToolClient.responseJson(context,json);
          }
        });
      }
    });
  }

  //todo 推荐,若没有参数的话,要创建 new ArrayList<Object>(1) 作为第2个参数参数
  public final void queryList(final String sql,final List<Object> params,final QueryResultList resultList){
    getPool().getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(Tuple.wrap(params),rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final ArrayList<JsonObject> list = new ArrayList<>();
            final RowSet<Row> rowSet = rows.result();
            final List<String> columns = rowSet.columnsNames();
            rowSet.forEach((item) ->{
              final JsonObject jsonObject = new JsonObject();
              for(int i = 0; i < columns.size(); i++){
                final String column = columns.get(i);
                jsonObject.put(column,item.getValue(column));
              }
              list.add(jsonObject);
            });
            //操作数据库成功
            resultList.succeed(list);
          }else{
            resultList.failure(rows.cause());
          }
        });
      }
    });
  }

  public final void queryMap(final RoutingContext context,final String sql){
    getPool().getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final JsonObject jsonObject = new JsonObject();
            final RowSet<Row> rowSet = rows.result();
            final List<String> columns = rowSet.columnsNames();
            rowSet.forEach((item) ->{
              for(int i = 0; i < columns.size();i++){
                final String column = columns.get(i);
                jsonObject.put(column,item.getValue(column));
              }
            });
            ToolClient.responseJson(context,ToolClient.queryJson(jsonObject));
          }else{
            logger.error("queryMap()出现异常,连接数据库失败:"+sql);
            final String json = ToolClient.createJson(199,"连接数据库失败");
            ToolClient.responseJson(context,json);
          }
        });
      }
    });
  }

  //不推荐使用,请使用上面的方法
  public final void queryMap(final RoutingContext context,final String sql,final List<Object> params){
    getPool().getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(Tuple.wrap(params),rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final JsonObject jsonObject = new JsonObject();
            final RowSet<Row> rowSet = rows.result();
            final List<String> columns = rowSet.columnsNames();
            rowSet.forEach((item) ->{
              for(int i = 0; i < columns.size();i++){
                final String column = columns.get(i);
                jsonObject.put(column,item.getValue(column));
              }
            });
            ToolClient.responseJson(context,ToolClient.queryJson(jsonObject));
          }else{
            logger.error("queryMap()出现异常,连接数据库失败:"+sql);
            final String json = ToolClient.createJson(199,"连接数据库失败");
            ToolClient.responseJson(context,json);
          }
        });
      }
    });
  }

  //若没有参数的话,要创建 new ArrayList<Object>(1) 作为第2个参数参数
  public final void queryMap(final String sql,final List<Object> params,final QueryResultMap resultMap){
    getPool().getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(Tuple.wrap(params),rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final JsonObject jsonObject = new JsonObject();
            final RowSet<Row> rowSet = rows.result();
            System.out.println(rowSet);
            final List<String> columns = rowSet.columnsNames();
            rowSet.forEach((item) ->{
              for(int i = 0; i < columns.size();i++){
                final String column = columns.get(i);
                jsonObject.put(column,item.getValue(column));
              }
            });
            resultMap.succeed(jsonObject);
          }else{
            resultMap.failure(rows.cause());
          }
        });
      }
    });
  }

  protected void failure(final RoutingContext context,final Throwable throwable){
    final String message = throwable.getMessage();
    if(message.contains("cannot be null")){
      ToolClient.responseJson(context,ToolClient.jsonParams());
    }else if(message.contains("Duplicate entry")){
      ToolClient.responseJson(context,ToolClient.createJson(199,"数据已存在"));
    }else{
      ToolClient.responseJson(context,ToolClient.jsonFailure());
    }
  }

  //不推荐使用,请使用上面的方法
  protected final void queryHashMap(final RoutingContext context,final String sql,final List<Object> params){
    getCon().compose(connection -> getRows(connection,sql,params)).onSuccess(rowSet ->{
      final List<String> columns = rowSet.columnsNames();
      final JsonObject jsonObject = new JsonObject();
      rowSet.forEach(item ->{
        for(int i = 0; i < columns.size(); i++){
          final String column = columns.get(i);
          jsonObject.put(column,item.getValue(column));
        }
      });
      ToolClient.responseJson(context,ToolClient.queryJson(jsonObject));
    }).onFailure(throwable ->{
      logger.error("queryListData()获取数据出现异常",throwable);
      ToolClient.responseJson(context,ToolClient.jsonFailure());
    });
  }

  //不推荐使用,请使用上面的方法
  protected final void queryListData(final RoutingContext context,final String sql,final List<Object> params){
    getCon().compose(connection -> getRows(connection,sql,params)).onSuccess(rowSet ->{
      final List<String> columns = rowSet.columnsNames();
      final ArrayList<JsonObject> list = new ArrayList<>();
      rowSet.forEach(item ->{
        final JsonObject jsonObject = new JsonObject();
        for(int i = 0; i < columns.size(); i++){
          final String column = columns.get(i);
          jsonObject.put(column,item.getValue(column));
        }
        list.add(jsonObject);
      });
      ToolClient.responseJson(context,ToolClient.queryJson(list));
    }).onFailure(throwable ->{
      logger.error("queryListData()获取数据出现异常",throwable);
      ToolClient.responseJson(context,ToolClient.jsonFailure());
    });
  }

  // ①获取数据库连接,通过链式调用;异步+响应式的链式调用示例,有且只有包含 Handler + AsyncResult 才能封装成链式调用
  private Future<SqlConnection> getCon(){
    final Promise<SqlConnection> promise = Promise.promise();
    getPool().getConnection(asyncResult ->{
      if(asyncResult.succeeded()){
        promise.complete(asyncResult.result());//重点,固定写法
      }else{
        promise.fail(asyncResult.cause());//重点,固定写法
      }
    });
    return promise.future();//重点,固定写法
  }

  // ②用获取到的连接查询数据库
  private Future<RowSet<Row>> getRows(final SqlConnection connection,final String sql,final List<Object> params){
    final Promise<RowSet<Row>> promise = Promise.promise();
    connection.preparedQuery(sql).execute(Tuple.wrap(params),handler ->{
      if(handler.succeeded()){
        promise.complete(handler.result());//重点,固定写法
      }else{
        promise.fail(handler.cause());//重点,固定写法
      }
    });
    return promise.future();//重点,固定写法
  }

  //拼接分页参数,注意添加顺序
  public final List<Object> pageParams(final Integer pageIndex,Integer pageSize){
    pageSize = (pageSize > 100) ? ConfigFiles.pageSize : pageSize;
    final Integer section = (pageIndex - 1) * pageSize;
    final ArrayList<Object> params = new ArrayList<>();
    params.add(section);
    params.add(pageSize);
    return params;
  }
}