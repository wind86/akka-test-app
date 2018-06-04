package com.testapp.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table("items")
public class ItemEntity implements Serializable {

  private static final long serialVersionUID = -7520880593330521023L;

  @PrimaryKeyColumn(name = "id", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
  private UUID id;

  @PrimaryKeyColumn(name = "threadName", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
  private String threadName;

  @Column("createdAt")
  private Date createdAt;

  public UUID getId() {
    return id;
  }
  public void setId(final UUID id) {
    this.id = id;
  }
  public String getThreadName() {
    return threadName;
  }
  public void setThreadName(final String name) {
    this.threadName = name;
  }
  public Date getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(final Date createdAt) {
    this.createdAt = createdAt;
  }
}