package com.fwtai.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * 自定义认证
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-09-30 14:21
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 <url>http://www.yinlz.com</url>
*/
public class AurhInfoProvider implements AuthProvider{

  @Override
  public void authenticate(final JsonObject authInfo,final Handler<AsyncResult<User>> resultHandler){
    // authInfo中存储了认证需要的相关信息，由调用者传入
    final String username = authInfo.getString("username");
    final String password = authInfo.getString("password");
    // 判断用户名和密码是否正确
    if("admin".equals(username) && "admin".equals(password)){
      // 密码验证通过，需要实例化授权对象，并在Future中响应给调用者
      // 实例化授权对象，可以将认证信息传入
      final User user = new AuthUser(authInfo);
      // 所有情况均成功返回，并将授权对象响应回去
      resultHandler.handle(Future.succeededFuture(user));
    }else{
      // 密码验证不通过，响应认证失败
      resultHandler.handle(Future.failedFuture("auth failure"));
    }
  }
}