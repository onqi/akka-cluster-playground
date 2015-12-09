package io.onqi.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class ClusterMain {
  public static void main(String[] args) {
    createWorker(new String[]{"2551"});
    createWorker(new String[]{"2552"});
    createWorker(new String[]{});
  }

  private static void createWorker(String[] args) {
    final String port = args.length > 0 ? args[0] : "0";
    final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
            withFallback(ConfigFactory.parseString("akka.cluster.roles = [worker]")).
            withFallback(ConfigFactory.load("cluster"));

    ActorSystem system = ActorSystem.create("ClusterSystem", config);

    system.actorOf(Props.create(WorkerActor.class), "worker");
  }
}
