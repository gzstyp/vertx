package com.fwtai.service;

import com.fwtai.tool.ToolClient;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class IndexHandle implements Handler<RoutingContext>{

  @Override
  public void handle(final RoutingContext context){
    ToolClient.responseJson(context,ToolClient.jsonSucceed("分发到具体的类的方法上"));
  }
}