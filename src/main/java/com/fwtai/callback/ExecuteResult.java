package com.fwtai.callback;

/**
 * insert,delete,update
 * @param
 * @作者 田应平
 * @QQ 444141300
 * @创建时间 2021/5/15 20:54
*/
public interface ExecuteResult{

  public void success(final int rows);
  public void failure(final Throwable throwable);
}