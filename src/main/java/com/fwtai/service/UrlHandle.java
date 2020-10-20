package com.fwtai.service;

import com.fwtai.tool.ToolClient;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.RoutingContext;

public class UrlHandle implements Handler<RoutingContext>{

  private final Vertx vertx;

  public UrlHandle(final Vertx vertx){
    this.vertx = vertx;
  }

  @Override
  public void handle(final RoutingContext context){
    final HttpClient httpClient = vertx.createHttpClient();
    //Function<HttpClientResponse,Future<HttpClientRequest>> handler = httpClient.redirectHandler();
    httpClient.redirectHandler((response -> {
      response.statusCode();
      return null;
    }));
    ToolClient.responseJson(context,ToolClient.jsonSucceed("分发到具体的类的方法上"));
  }
}