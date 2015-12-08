package io.onqi.cluster;

public class ClusterMain {
  public static void main(String[] args) {
    WorkerActor.main(new String[]{"2551"});
    WorkerActor.main(new String[]{"2552"});
    WorkerActor.main(new String[]{});
  }
}
