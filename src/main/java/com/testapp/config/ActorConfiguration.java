package com.testapp.config;

import static com.testapp.actor.extension.SpringExtension.SPRING_EXTENSION_PROVIDER;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.testapp.actor.ApplicationActor;
import com.testapp.repository.ItemRepository;

import akka.actor.ActorSystem;

@Configuration
@ComponentScan(basePackages = { "com.testapp.actor" })
public class ActorConfiguration {

  public static final String ACTOR_SYSTEM_NAME = "actorSystem";
  public static final String APPLICATION_ACTOR = "applicationActor";

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private ApplicationArguments applicationArguments;

  @Autowired
  private ItemRepository itemRepository;

  @Bean
  public ActorSystem actorSystem() {
    final ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);
    system.actorOf(ApplicationActor.props(itemRepository, getThreadsLimit(), getItemLimit()), APPLICATION_ACTOR);

    SPRING_EXTENSION_PROVIDER.get(system).initialize(applicationContext);
    return system;
  }

  private int getThreadsLimit() {
    final List<String> args = applicationArguments.getNonOptionArgs();
    final int threadsLimit = NumberUtils.toInt(args.get(0));

    if (threadsLimit < 1 || threadsLimit > 64) {
      throw new IllegalArgumentException("invalid thread limit. please, choose any number between 1 and 64 inclusively");
    }

    return threadsLimit;
  }

  private long getItemLimit() {
    final List<String> args = applicationArguments.getNonOptionArgs();
    return Long.parseLong(args.get(1));
  }
}
