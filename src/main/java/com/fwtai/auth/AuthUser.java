package com.fwtai.auth;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class AuthUser implements User{

  private JsonObject authInfo;

  public AuthUser(final JsonObject authInfo){
    this.authInfo = authInfo;
  }

  /**
   * 这里依然是通过resultHandle响应授权信息，返回值为当前对象是为了Fluent调用模式
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