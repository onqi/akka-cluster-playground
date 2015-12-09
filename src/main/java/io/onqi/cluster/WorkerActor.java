package io.onqi.cluster;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class WorkerActor extends UntypedActor {
  private final LoggingAdapter log = Logging.getLogger(context().system(), this);
  private final List<String> strings = new ArrayList<>();
  private final UUID actorId = UUID.randomUUID();

  public static Props createProps() {return Props.create(WorkerActor.class); }


  @Override
  public void onReceive(Object message) throws Exception {
    log.info("Got message  {}", message);
    if (message instanceof StoreStringMessage) {
      handleStoreRequest((StoreStringMessage) message);
    } else {
      unhandled(message);
    }
  }

  @Override
  public void preStart() throws Exception {
    log.info("Starting worker {}", actorId);
  }

  public void handleStoreRequest(StoreStringMessage sr) {
    log.info("{}: Storing the message {}", actorId, sr);
    strings.add(sr.string);
    sender().tell("Ack", ActorRef.noSender());
  }

  public void handleGetRequest(GetStringMessage gsm) {
    if (strings.contains(gsm.string)) {
      log.info("{}: I have that string!", actorId);
      sender().tell(new StringMessage(gsm.string), self());
    } else {
      log.info("{}: I don't have that string", actorId);
    }
  }

  public static class StoreStringMessage implements Serializable {
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

  public static class GetStringMessage implements Serializable {
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
