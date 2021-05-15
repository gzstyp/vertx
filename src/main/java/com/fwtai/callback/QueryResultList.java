package com.fwtai.callback;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * 查询多条数据
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2021/5/15 20:56
 * @QQ号码 444141300
 * @Email service@yinlz.com
 * @官网 <url>http://www.yinlz.com</url>
*/
public interface QueryResultList{

  public void succeed(final ArrayList<JsonObject> list);
  public void failure(final Throwable throwable);
}