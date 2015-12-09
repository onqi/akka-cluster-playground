package io.onqi.cluster;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.FromConfig;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class ClusterParent extends AbstractActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ActorRef workerRouter = getContext().actorOf(FromConfig.getInstance().props(WorkerActor.createProps()),
          "workerRouter");

  public static Props createProps() {
    return Props.create(ClusterParent.class);
  }

  @Override
  public PartialFunction<Object, BoxedUnit> receive() {
    return ReceiveBuilder
            .match(WorkerActor.StoreStringMessage.class, this::storeMessage)
            .matchAny(this::unhandled)
            .build();
  }

  private void storeMessage(WorkerActor.StoreStringMessage message) {
    log.info("Got message {}", message);
    workerRouter.tell(message, sender());
  }
}
