package com.testapp.actor.extension;

import org.springframework.context.ApplicationContext;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;

public class SpringActorProducer implements IndirectActorProducer {

  private final ApplicationContext applicationContext;

  private final String beanActorName;

  public SpringActorProducer(final ApplicationContext applicationContext, final String beanActorName) {
    this.applicationContext = applicationContext;
    this.beanActorName = beanActorName;
  }

  @Override
  public Actor produce() {
    return (Actor) applicationContext.getBean(beanActorName);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<? extends Actor> actorClass() {
    return (Class<? extends Actor>) applicationContext.getType(beanActorName);
  }
}