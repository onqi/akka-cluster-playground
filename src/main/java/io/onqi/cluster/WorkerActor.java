package io.onqi.cluster;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class WorkerActor extends AbstractActor {
  private final LoggingAdapter log = Logging.getLogger(context().system(), this);
  private final List<String> strings = new ArrayList<>();
  private final UUID actorId = UUID.randomUUID();

  public static Props createProps() {return Props.create(WorkerActor.class); }

  public static void main(String[] args) {
    final String port = args.length > 0 ? args[0] : "0";
    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
            withFallback(ConfigFactory.parseString("akka.cluster.roles = [worker]")).
            withFallback(ConfigFactory.load("cluster"));

    ActorSystem system = ActorSystem.create("ClusterSystem", config);

    system.actorOf(Props.create(WorkerActor.class), "worker");

    system.actorOf(Props.create(MetricsListener.class), "metricsListener");
  }

  @Override
  public PartialFunction<Object, BoxedUnit> receive() {
    return ReceiveBuilder
            .matchAny((message) -> log.info("Received message {}", message))
            .match(StoreStringMessage.class, this::handleStoreRequest)
            .match(GetStringMessage.class, this::handleGetRequest)
            .matchAny(this::unhandled)
            .build();
  }

  @Override
  public void preStart() throws Exception {
    log.info("Starting worker {}", actorId);
  }

  public void handleStoreRequest(StoreStringMessage sr) {
    log.info("{}: Storing the message", actorId);
    strings.add(sr.string);
  }

  public void handleGetRequest(GetStringMessage gsm) {
    if (strings.contains(gsm.string)) {
      log.info("{}: I have that string!", actorId);
      sender().tell(new StringMessage(gsm.string), self());
    }
  }

  public static class StoreStringMessage {
    private static final long serialVersionUID = 1L;

    private final String string;

    public StoreStringMessage(String string) {
      this.string = string;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      StoreStringMessage that = (StoreStringMessage) o;
      return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
      return Objects.hash(string);
    }

    @Override
    public String toString() {
      return "StoreStringMessage{" +
              "string='" + string + '\'' +
              '}';
    }
  }

  public static class GetStringMessage {
    private static final long serialVersionUID = 1L;
    private final String string;

    public GetStringMessage(String string) {
      this.string = string;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GetStringMessage that = (GetStringMessage) o;
      return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
      return Objects.hash(string);
    }

    @Override
    public String toString() {
      return "GetStringMessage{" +
              "string='" + string + '\'' +
              '}';
    }
  }

  public static class StringMessage {
    private static final long serialVersionUID = 1L;

    private final String string;

    private StringMessage(String string) {
      this.string = string;
    }

    public String getString() {
      return string;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      StringMessage that = (StringMessage) o;
      return Objects.equals(string, that.string);
    }

    @Override
    public int hashCode() {
      return Objects.hash(string);
    }

    @Override
    public String toString() {
      return "StringMessage{" +
              "string='" + string + '\'' +
              '}';
    }
  }
}
