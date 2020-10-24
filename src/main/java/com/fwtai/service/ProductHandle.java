package com.fwtai.service;

import com.fwtai.tool.ToolClient;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class ProductHandle implements Handler<RoutingContext>{

  private final Vertx vertx;

  public ProductHandle(final Vertx vertx){
    this.vertx = vertx;
  }

  @Override
  public void handle(final RoutingContext context){
    final String page = context.request().getParam("page");
    final String size = context.request().getParam("size");
    ToolClient.responseSucceed(context,page+",二级路由-list方法,"+size);
  }
}