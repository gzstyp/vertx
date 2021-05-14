package com.fwtai.callback;

import io.vertx.core.json.JsonObject;

public interface ExecuteResult{

  public void succeed(final JsonObject jsonObject);
  public void failure(final Throwable throwable);
}