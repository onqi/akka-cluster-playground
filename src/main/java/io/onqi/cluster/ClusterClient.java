package io.onqi.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.Console;

import java.util.concurrent.TimeUnit;

public class ClusterClient {
  private static LoggingAdapter log;

  public static void main(String[] args) {
    final Config config = ConfigFactory.parseString("akka.cluster.roles = [sender]")
            .withFallback(ConfigFactory.load("cluster"));

    ActorSystem system = ActorSystem.create("ClusterSystem", config);
    log = Logging.getLogger(system, ClusterClient.class);

    Cluster cluster = Cluster.get(system);
    ActorRef clusterParent = system.actorOf(ClusterParent.createProps(), "clusterParent");
    doMessageLoop(system, cluster, clusterParent);
  }

  @SuppressWarnings("unchecked")
  private static void doMessageLoop(ActorSystem system, Cluster cluster, ActorRef workerRouter) {
    String message;

    while (!(message = Console.readLine().toString()).isEmpty()) {
      workerRouter.tell(new WorkerActor.StoreStringMessage(message), ActorRef.noSender());
      Patterns.ask(workerRouter, new WorkerActor.GetStringMessage(message),
              Timeout.apply(300, TimeUnit.MILLISECONDS))
              .onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable failure, Object success) throws Throwable {
                  if (failure != null) {
                    log.info("Got response {}", success);
                  } else {
                    log.info("Got failure {}", success);
                  }
                }
              }, system.dispatcher());
    }
    cluster.shutdown();
  }
}
