package org.telecom.slr.experiments;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import org.telecom.slr.actor.Process;
import org.telecom.slr.actor.messages.Deactivate;
import org.telecom.slr.actor.messages.ReadMessage;
import org.telecom.slr.actor.messages.WriteMessage;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Experiment implements Runnable {
    private final ExperimentsModel model;
    private ActorSystem system;
    private ActorRef listener;

    public Experiment(ExperimentsModel model) {
        this.model = model;
    }

    private void setUp() {
        this.system = ActorSystem.create("actorSystem");
        this.listener = system.actorOf(
                Props.create(ExperimentResultCollectorActor.class,
                ExperimentResultCollectorActor::new), "result");
    }

    private void execute(Integer numberOfProcess, Integer numberOfDeactivateProcess, Integer numberOfMessages) throws InterruptedException {
        List<ActorRef> nodes =  new LinkedList<>();

        for (int i = 0; i < numberOfProcess; i++) {
            ActorRef actor = this.system.actorOf(Props.create(Process.class, Process::new), "node"+i);
            nodes.add(actor);
        }

        for (int i = 0; i < numberOfProcess; i++) {
            for (int j = 0; j < numberOfProcess; j++) {
                if (i != j) {
                    nodes.get(i).tell(nodes.get(j), ActorRef.noSender());
                }
            }
        }

        Thread.sleep(500);

        List<ActorRef> deactivatedNodes = new LinkedList<>(nodes);
        Collections.shuffle(deactivatedNodes);

        for (int i = 0; i < numberOfDeactivateProcess; i++) {
            deactivatedNodes.get(i).tell(new Deactivate(), ActorRef.noSender());
        }

        Thread.sleep(500);
        ExperimentResultCollectorActor.fromNowOnCollect(numberOfProcess, numberOfMessages);
        for (int i = 1; i <= numberOfProcess; i++) {
            for (int j = 1; j <= numberOfMessages; j++) {
                nodes.get(i-1).tell(new WriteMessage(i + numberOfProcess*j), this.listener);
                nodes.get(i-1).tell(new ReadMessage(), this.listener);
            }
        }
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < model.numbersOfProcess.size(); i++) {
                setUp();
                execute(model.numbersOfProcess.get(i),
                        model.numbersOfDeactivatedProcess.get(i),
                        model.numbersOfMessages.get(i));
                tearDown();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void tearDown() throws InterruptedException {
        Thread.sleep(500);
        this.system.terminate();
    }
}
