package org.telecom.slr;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.telecom.slr.actor.Process;

public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("system");
        final ActorRef nodeA = system.actorOf(Props.create(Process.class, Process::new), "nodeA");

        for (char n = 'B'; n <= 'Z'; n++) {
            ActorRef node = system.actorOf(Props.create(Process.class, Process::new), "node"+n);
            nodeA.tell(node, ActorRef.noSender());
        }

        for (int n = 0; n <= 1000; n++) {
            ActorRef node = system.actorOf(Props.create(Process.class, Process::new), "node"+n);
            nodeA.tell(node, ActorRef.noSender());
        }

        nodeA.tell("start", ActorRef.noSender());

        new Thread(() -> {
            try {
                Thread.sleep(6000);
                system.terminate();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}