package com.fwtai.bean;

import io.vertx.core.json.JsonObject;

public final class User{

  private String kid;
  private String name;
  private Integer age;

  public User(){}

  public User(String kid,String name,Integer age){
    this.kid = kid;
    this.name = name;
    this.age = age;
  }

  public User(final JsonObject jsonObject){
    this.kid = jsonObject.getString("kid");
    this.name = jsonObject.getString("name");
    this.age = jsonObject.getInteger("age");
  }
}