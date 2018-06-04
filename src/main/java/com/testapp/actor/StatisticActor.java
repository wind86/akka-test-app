package com.testapp.actor;

import java.util.List;

import com.testapp.actor.ApplicationActor.StopApplication;
import com.testapp.entity.StatisticItemEntity;
import com.testapp.repository.ItemRepository;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class StatisticActor extends AbstractActor {

  private static final String STATISTIC_FORMAT = "%s-%d-%d";

  public static Props props(final ItemRepository itemRepository) {
    return Props.create(StatisticActor.class, itemRepository);
  }

  public static class DisplayStatistic {};

  private final ItemRepository itemRepository;

  public StatisticActor(final ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(DisplayStatistic.class, r -> {
          displayStatistic(itemRepository.calculateStatistics());
          getContext().getParent().tell(new StopApplication(), ActorRef.noSender());
        })
        .build();
  }

  private void displayStatistic(final List<StatisticItemEntity> statisticItems) {
    final long totalCounter = statisticItems.stream().mapToLong(item -> item.getItemsCount()).sum();

    statisticItems.forEach(stat -> {
      System.out.println(String.format(STATISTIC_FORMAT, stat.getThreadName(), stat.getItemsCount(), totalCounter));
    });
  }
}