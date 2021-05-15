package com.fwtai.callback;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public interface QueryResultList{

  public void succeed(final ArrayList<JsonObject> list);
  public void failure(final Throwable throwable);
}