package com.testapp.actor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.testapp.actor.ExecutionActor.ExecutionException;
import com.testapp.actor.ExecutionActor.StartExecution;
import com.testapp.actor.ExecutionActor.StopExecution;
import com.testapp.actor.StatisticActor.DisplayStatistic;
import com.testapp.repository.ItemRepository;

import akka.actor.AbstractActor;
import akka.actor.ActorKilledException;
import akka.actor.ActorRef;
import akka.actor.Kill;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

public class ApplicationActor extends AbstractActor {

  public static Props props(final ItemRepository itemRepository, final int threadsLimit, final long itemsLimit) {
    return Props.create(ApplicationActor.class, itemRepository, threadsLimit, itemsLimit);
  }

  public static class StartApplication {}
  public static class StopApplication {}
  public static class ShowStatistic {}

  private final List<ActorRef> executionActors;
  private final ActorRef statisticActor;

  private static final SupervisorStrategy SUPERVISOR_STRATEGY = new OneForOneStrategy(DeciderBuilder
      .match(ExecutionException.class, e -> SupervisorStrategy.restart())
      .match(ActorKilledException.class, e -> SupervisorStrategy.stop())
      .matchAny(o -> SupervisorStrategy.escalate())
      .build());

  public ApplicationActor(final ItemRepository itemRepository, final int threadsLimit, final long itemsLimit) {
    executionActors = IntStream.range(1, threadsLimit + 1).mapToObj(i -> {
      return getContext().actorOf(ExecutionActor.props(itemRepository, itemsLimit), "executor_" + i);
    }).collect(Collectors.toList());

    statisticActor = getContext().actorOf(StatisticActor.props(itemRepository), "statistic");
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return SUPERVISOR_STRATEGY;
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
        .match(StartApplication.class, r -> {
          executionActors.forEach(actor -> actor.tell(new StartExecution(), getSelf()));
        })
        .match(ShowStatistic.class, r -> {
          executionActors.forEach(actor -> {
            actor.tell(new StopExecution(), getSelf());
            actor.tell(Kill.getInstance(), getSelf());
          });
          statisticActor.tell(new DisplayStatistic(), getSelf());
        })
        .match(StopApplication.class, r -> {
          getContext().getSystem().terminate();
          System.exit(0);
        })
        .build();
  }
}