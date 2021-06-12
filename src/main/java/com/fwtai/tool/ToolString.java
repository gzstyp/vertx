package com.fwtai.tool;

import io.netty.util.internal.ThreadLocalRandom;

import java.util.UUID;

public class ToolString{

  /**多线程下生成32位唯一的字符串*/
  public static String getIdsChar32(){
    final ThreadLocalRandom random = ThreadLocalRandom.current();
    return new UUID(random.nextInt(),random.nextInt()).toString().replaceAll("-","");
  }
}