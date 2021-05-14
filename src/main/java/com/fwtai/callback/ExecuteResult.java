package com.fwtai.callback;

import io.vertx.core.json.JsonObject;

public interface ExecuteResult{

  void succeed(final JsonObject jsonObject);
  void failure(final Throwable throwable);
}