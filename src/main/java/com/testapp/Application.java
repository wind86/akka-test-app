package com.testapp;

import static com.testapp.config.ActorConfiguration.APPLICATION_ACTOR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.testapp.actor.ApplicationActor.ShowStatistic;
import com.testapp.actor.ApplicationActor.StartApplication;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

@SpringBootApplication
public class Application implements CommandLineRunner {

  @Autowired
  private ActorSystem actorSystem;

  public static void main(final String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(final String... args) throws Exception {
    final ActorSelection application = actorSystem.actorSelection("/user/" + APPLICATION_ACTOR);
    application.tell(new StartApplication(), ActorRef.noSender());

    System.out.println("Press ENTER to exit the system");
    System.in.read();

    application.tell(new ShowStatistic(), ActorRef.noSender());
  }
}