package org.telecom.slr.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.telecom.slr.actor.messages.Send;
import org.telecom.slr.actor.messages.WriteMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Node extends Actor {
    private final List<ActorRef> address = new LinkedList<>();
    private int numberOfRequests = 0;
    private Queue<WriteMessage> writeQueue = new LinkedList<>();

    public Node() {
        run(this::log);
        run(this::getRef).when(message -> message instanceof ActorRef);
        run(this::write).when(message -> message instanceof WriteMessage);
    }

    private void write(Object message, AbstractActor.ActorContext context) {
        numberOfRequests++;
        writeQueue.add((WriteMessage) message);
        sendToAll(new Send(numberOfRequests));
    }

    private void sendToAll(Object message) {
        address.forEach(ref -> ref.tell(message, self()));
    }

    private void getRef(Object message, AbstractActor.ActorContext context) {
        ActorRef ref = (ActorRef) message;
        address.add(ref);
    }

    private void log(Object message, AbstractActor.ActorContext context) {
        String from = context.sender().path().name();
        String to = context.self().path().name();

        System.out.printf("from %s to %s : %s%n", from, to, message);
    }
}