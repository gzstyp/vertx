package com.fwtai.auth;

import com.fwtai.tool.ToolClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

/**
 * 提供两种认证方式,一种是用户名和密码认证;一种是token认证
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-09-30 15:20
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 <url>http://www.yinlz.com</url>
*/
public final class JwtAuthVerticle extends AbstractVerticle {

  private final JWTAuthOptions config = new JWTAuthOptions()
    .addPubSecKey(
      new PubSecKeyOptions()
      .setAlgorithm("HS256")
      .setPublicKey("Www_Yinlz0Com2020DWC.cloud")
      .setSymmetric(true)
    );

  private final JWTAuth jwtAuth = JWTAuth.create(vertx,config);

  @Override
  public void start() throws Exception {

    final HttpServer server = vertx.createHttpServer();

    // 处理客户端请求,http://192.168.3.108:8080/?username=admin&password=admin 或 192.168.3.108:8080/?token=xxxx
    server.requestHandler(request -> {
      final JsonObject jsonObject = parseQuery(request.query());
      if(jsonObject == null){
        final String jsonFailure = ToolClient.jsonFailure("参数有误");
        ToolClient.getResponse(request).end(jsonFailure);
        return;
      };
      // 判断用户是否带token来认证，如果带token，就直接通过token来认证，否则认为是第一次认证，通过用户名和密码的方式进行认证
      final String token = jsonObject.getString("token");
      if(token == null || token.length() <= 0){
        // 先使用默认的用户名密码进行认证
        final AurhInfoProvider provider = new AurhInfoProvider();
        provider.authenticate(jsonObject, auth -> {
          if (auth.succeeded()) {
            // 认证通过之后，再生成token，以后就使用token进行认证
            final JsonObject data = new JsonObject().put("userId","admin");
            final String jsonData = jwtAuth.generateToken(data);
            ToolClient.getResponse(request).end(ToolClient.queryJson(jsonData));
            return;
          } else {
            final String jsonFailure = ToolClient.jsonFailure("认证失败,用户名或密码错误");
            ToolClient.getResponse(request).end(jsonFailure);
          }
        });
      } else {
        // 使用jwt进行认证
        jwtAuth.authenticate(new JsonObject().put("jwt",token), auth -> {
          if (auth.succeeded()) {
            final User user = auth.result();
            final JsonObject authData = user.principal();
            final String userId = authData.getString("userId");
            final String jsonSucceed = ToolClient.jsonSucceed("认证成功,用户名:"+userId);
            ToolClient.getResponse(request).end(jsonSucceed);
          } else {
            final String jsonFailure = ToolClient.jsonFailure("认证失败,token无效");
            ToolClient.getResponse(request).end(jsonFailure);
          }
        });
      }
    });
    server.listen(8080);
  }

  /**
   * 把URL后跟的查询字符串转成json对象
   *
   * @param query
   * @return
  */
  public JsonObject parseQuery(final String query){
    if(query == null || query.length() <= 0) return null;
    final JsonObject data = new JsonObject();
    final String[] params = query.split("&");
    for (String param : params) {
      final String[] k = param.split("=");
      if(k.length >= 2){
        final String key = k[0];
        final String value = k[1];
        if(value != null && value.trim().length() > 0){
          data.put(key,value);
        }
      }
    }
    return data;
  }

  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new JwtAuthVerticle());
  }
}