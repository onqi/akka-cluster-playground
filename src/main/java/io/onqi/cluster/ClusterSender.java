package io.onqi.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.Console;

import java.util.concurrent.TimeUnit;


public class ClusterSender {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("ClusterSystem");
    Cluster cluster = Cluster.get(system);
    ActorSelection workerRouter = system.actorSelection("/cluster/workerRouter");
    String message;
    final LoggingAdapter log = Logging.getLogger(system, ClusterSender.class);
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
