package com.fwtai.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * 类似于 org.springframework.security.core.userdetails.UserDetailsService
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020/10/26 14:16
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 <url>http://www.yinlz.com</url>
*/
public class AuthUser implements User{

  private final JsonObject authInfo;

  public AuthUser(final JsonObject authInfo){
    this.authInfo = authInfo;
  }

  /**
   * 经授权的；经认可的;这里依然是通过resultHandle响应授权信息，返回值为当前对象是为了Fluent调用模式
  */
  @Override
  public User isAuthorized(final String authority,final Handler<AsyncResult<Boolean>> resultHandler){
    // 一直返回成功
    resultHandler.handle(Future.succeededFuture(true));
    return this;
  }

  @Override
  public User clearCache(){
    return null;
  }

  @Override
  public JsonObject principal(){
    return authInfo;
  }

  @Override
  public void setAuthProvider(final AuthProvider authProvider){}
}