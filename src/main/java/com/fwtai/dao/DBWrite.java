package com.fwtai.dao;

import com.fwtai.callback.ExecuteResult;
import com.fwtai.tool.ToolClient;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.util.List;

/**
 * 写库
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021-02-04 9:45
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class DBWrite{

  final InternalLogger logger = Log4JLoggerFactory.getInstance(getClass());

  private final MySQLPool client;

  private final MySQLConnectOptions connectOptions;

  public DBWrite(final Vertx vertx){
    connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost("192.168.3.66")
      .setDatabase("vertx")
      .setUser("root")
      .setPassword("rootFwtai")
      .setCharset("utf8mb4")
      .setSsl(false);
    //配置数据库连接池
    final PoolOptions pool = new PoolOptions().setMaxSize(32);
    client = MySQLPool.pool(vertx,connectOptions,pool);
  }

  public MySQLPool getClient(){
    return client;
  }

  public final void exeSql(final RoutingContext context,final String sql){
    client.getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final RowSet<Row> rowSet = rows.result();
            final int count = rowSet.rowCount();
            ToolClient.responseJson(context,ToolClient.executeRows(count));
          }else{
            logger.error("exeSql()出现异常,执行sql:"+sql);
            failure(context,rows.cause());
          }
        });
      }
    });
  }

  public final void exeSql(final RoutingContext context,final String sql,final List<Object> params){
    client.getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(Tuple.wrap(params),rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final RowSet<Row> rowSet = rows.result();
            final int count = rowSet.rowCount();
            ToolClient.responseJson(context,ToolClient.executeRows(count));
          }else{
            failure(context,rows.cause());
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

  //todo 若没有参数的话,要创建 new ArrayList<Object>(0) 作为第2个参数
  protected void executeSQL(final String sql,final List<Object> params,final ExecuteResult executeResult){
    client.getConnection((result) -> {
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(Tuple.wrap(params),rows -> {
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            final RowSet<Row> rowSet = rows.result();
            final int count = rowSet.rowCount();
            executeResult.success(count);
          }else{
            executeResult.failure(rows.cause());
          }
        });
      }
    });
  }

  //todo 用法,daoHandle.execute(sql,params).onSuccess(handler->{}).onFailure(throwable->{});
  /* 示例:
    daoHandle.execute(sqlAdd,paramsAdd).onSuccess(rows->{
      final RowSet<Row> rowSet = rows.value();
      final int count = rowSet.rowCount();
      System.out.println("count->"+count);
      ToolClient.responseJson(context,ToolClient.executeRows(count));
    }).onFailure(err->{
      ToolClient.responseJson(context,ToolClient.createJson(199,"连接数据库失败"));
    });
  */
  public final Future<RowSet<Row>> execute(final String sql,final List<Object> params){
    final Promise<RowSet<Row>> promise = Promise.promise();
    client.getConnection((result) ->{
      if(result.succeeded()){
        final SqlConnection conn = result.result();
        conn.preparedQuery(sql).execute(Tuple.wrap(params),rows ->{
          conn.close();//推荐写在第1行,防止忘记释放资源
          if(rows.succeeded()){
            promise.complete(rows.result());//重点,固定写法
          }else{
            promise.fail(rows.cause());//重点,固定写法
          }
        });
      }
    });
    return promise.future();//重点,固定写法
  }
}