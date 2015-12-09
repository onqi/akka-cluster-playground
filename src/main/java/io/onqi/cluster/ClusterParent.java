package io.onqi.cluster;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.FromConfig;

public class ClusterParent extends UntypedActor {
  private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ActorRef workerRouter = getContext().actorOf(FromConfig.getInstance().props(WorkerActor.createProps()),
          "workerRouter");

  public static Props createProps() {
    return Props.create(ClusterParent.class);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    log.info("Got message  {}", message);
    if (message instanceof WorkerActor.StoreStringMessage) {
      routeStore((WorkerActor.StoreStringMessage) message);
    } else {
      unhandled(message);
    }
  }

  private void routeGet(WorkerActor.GetStringMessage message) {
    workerRouter.tell(message, sender());
    workerRouter.forward(message, context());
  }

  private void routeStore(WorkerActor.StoreStringMessage message) {
    workerRouter.tell(message, sender());
  }
}
