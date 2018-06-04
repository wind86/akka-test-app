package com.testapp.actor;

import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.RandomUtils;

import com.datastax.driver.core.utils.UUIDs;
import com.testapp.entity.ItemEntity;
import com.testapp.repository.ItemRepository;

import akka.actor.Props;
import akka.actor.UntypedAbstractActor;

public class ExecutionActor extends UntypedAbstractActor {

  public static Props props(final ItemRepository itemRepository, final long itemsLimit) {
    return Props.create(ExecutionActor.class, itemRepository, itemsLimit);
  }

  public static class StartExecution {
  }

  public static class StopExecution {
  }

  public static class ExecutionException extends Exception {

    public ExecutionException(final Throwable source) {
      super(source);
    }
  }

  private final ItemRepository itemRepository;
  private final long itemsLimit;

  private boolean isStopped;

  public ExecutionActor(final ItemRepository itemRepository, final long itemsLimit) {
    this.itemRepository = itemRepository;
    this.itemsLimit = itemsLimit;
  }

  @Override
  public void onReceive(final Object message) throws Throwable {
    if (message instanceof StartExecution) {
      execute();
    } else if (message instanceof StopExecution) {
      this.isStopped = true;
    }
  }

  private void execute() throws Throwable {
    try {
      itemRepository.save(createItem());

      final ItemEntity oldestItem = itemRepository.loadOldest();
      if (oldestItem != null && shouldRemoveOldestItem(oldestItem)) {
        itemRepository.delete(oldestItem);
      }

      if (!isStopped) {
        Thread.sleep(RandomUtils.nextLong(0, 100));

        getSelf().tell(new StartExecution(), getSender());
      }

    } catch (final Throwable t) {
      throw new ExecutionException(t);
    }
  }

  private boolean shouldRemoveOldestItem(final ItemEntity item) {
    return !isThreadNameMatch(getSelf().path().name(), item) || isItemLimitExceeds();
  }

  private boolean isThreadNameMatch(final String threadName, final ItemEntity item) {
    return item != null && Objects.equals(threadName, item.getThreadName());
  }

  private boolean isItemLimitExceeds() {
    return itemRepository.count() > itemsLimit;
  }

  private ItemEntity createItem() {
    final ItemEntity item = new ItemEntity();
    item.setId(UUIDs.timeBased());
    item.setThreadName(getSelf().path().name());
    item.setCreatedAt(new Date());
    return item;
  }
}