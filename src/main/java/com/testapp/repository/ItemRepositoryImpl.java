package com.testapp.repository;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.testapp.entity.ItemEntity;
import com.testapp.entity.StatisticItemEntity;

public class ItemRepositoryImpl implements ItemRepositoryCustom {

  @Autowired
  private CassandraOperations cassandraTemplate;

  @Override
  public ItemEntity loadOldest() {
    final Select selectQuery = QueryBuilder.select().from("items");
    selectQuery.allowFiltering();

    final Clause clause = QueryBuilder.eq("createdAt", loadMinCreatedAtDate());

    final Where selectWhere = selectQuery.where();
    selectWhere.and(clause);

    final List<ItemEntity> oldestItems = cassandraTemplate.query(selectQuery, (row, rowNum) -> {
      final ItemEntity ie = new ItemEntity();

      ie.setThreadName(row.getString(0));
      ie.setId(row.getUUID(1));
      ie.setCreatedAt(new Date(row.getDate(2).getMillisSinceEpoch()));

      return ie;
    });

    return CollectionUtils.isNotEmpty(oldestItems) ? oldestItems.get(0) : null;
  }

  @Override
  public Date loadMinCreatedAtDate() {
    return cassandraTemplate.queryForObject("select min(createdAt) from items", Date.class);
  }

  @Override
  public List<StatisticItemEntity> calculateStatistics() {
    return cassandraTemplate.query("select threadName, count(*) from items", (row, rowNum) -> {
      return new StatisticItemEntity(row.getString(0), row.getLong(1));
    });
  }
}