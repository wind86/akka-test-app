package com.testapp.entity;

public class StatisticItemEntity {

  private String threadName;
  private long itemsCount;

  public StatisticItemEntity(final String threadName, final long itemsCount) {
    this.threadName = threadName;
    this.itemsCount = itemsCount;
  }

  public String getThreadName() {
    return threadName;
  }
  public void setThreadName(final String threadName) {
    this.threadName = threadName;
  }
  public long getItemsCount() {
    return itemsCount;
  }
  public void setItemsCount(final long itemsCount) {
    this.itemsCount = itemsCount;
  }
}