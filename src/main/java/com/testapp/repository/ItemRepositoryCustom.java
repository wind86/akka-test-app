package com.testapp.repository;

import java.util.Date;
import java.util.List;

import com.testapp.entity.ItemEntity;
import com.testapp.entity.StatisticItemEntity;

public interface ItemRepositoryCustom {
  Date loadMinCreatedAtDate();

  ItemEntity loadOldest();

  List<StatisticItemEntity> calculateStatistics();
}
