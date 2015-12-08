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

  private ActorRef workerRouter = getContext().actorOf(FromConfig.getInstance().props(),
          "workerRouter");

  public static Props createProps() {
    return Props.create(ClusterParent.class);
  }

  @Override
  public PartialFunction<Object, BoxedUnit> receive() {
    return ReceiveBuilder
            .match(WorkerActor.GetStringMessage.class, this::routeGet)
            .match(WorkerActor.StoreStringMessage.class, this::routeStore)
            .matchAny(this::unhandled)
            .build();
  }


  private void routeGet(WorkerActor.GetStringMessage message) {
    workerRouter.forward(message, context());
  }

  private void routeStore(WorkerActor.StoreStringMessage message) {
    workerRouter.forward(message, context());

  }
}
