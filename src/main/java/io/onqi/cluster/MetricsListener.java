package io.onqi.cluster;

import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.NodeMetrics;
import akka.cluster.StandardMetrics;
import akka.cluster.metrics.ClusterMetricsExtension;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MetricsListener extends UntypedActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  Cluster cluster = Cluster.get(getContext().system());

  ClusterMetricsExtension extension = ClusterMetricsExtension.get(getContext().system());


  // Subscribe unto ClusterMetricsEvent events.
  @Override
  public void preStart() {
    extension.subscribe(getSelf());
  }

  // Unsubscribe from ClusterMetricsEvent events.
  @Override
  public void postStop() {
    extension.unsubscribe(getSelf());
  }


  @Override
  public void onReceive(Object message) {
    if (message instanceof ClusterEvent.ClusterMetricsChanged) {
      ClusterEvent.ClusterMetricsChanged clusterMetrics = (ClusterEvent.ClusterMetricsChanged) message;
      for (NodeMetrics nodeMetrics : clusterMetrics.getNodeMetrics()) {
        if (nodeMetrics.address().equals(cluster.selfAddress())) {
          logHeap(nodeMetrics);
          logCpu(nodeMetrics);
        }
      }

    } else if (message instanceof ClusterEvent.CurrentClusterState) {
      // Ignore.
    } else {
      unhandled(message);
    }
  }

  void logHeap(NodeMetrics nodeMetrics) {
    StandardMetrics.HeapMemory heap = StandardMetrics.extractHeapMemory(nodeMetrics);
    if (heap != null) {
      log.info("Used heap: {} MB", ((double) heap.used()) / 1024 / 1024);
    }
  }

  void logCpu(NodeMetrics nodeMetrics) {
    StandardMetrics.Cpu cpu = StandardMetrics.extractCpu(nodeMetrics);
    if (cpu != null && cpu.systemLoadAverage().isDefined()) {
      log.info("Load: {} ({} processors)", cpu.systemLoadAverage().get(),
              cpu.processors());
    }
  }

}