package org.telecom.slr.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.telecom.slr.actor.messages.SendMessage;
import org.telecom.slr.actor.messages.UpdateMessage;
import org.telecom.slr.actor.messages.ValueMessage;
import org.telecom.slr.actor.messages.WriteMessage;

import java.util.*;

public class Node extends Actor {
    private final List<ActorRef> address = new LinkedList<>();
    private Map<Integer, WriteRequest> writeRequests = new HashMap<>();

    private int numberOfRequests = 0;
    private int timeStamp = -1;
    private int value = -1;

    public Node() {
        run(this::log);
        run(this::getRef).when(message -> message instanceof ActorRef);
        run(this::write).when(message -> message instanceof WriteMessage);
        run(this::sendToSender).when(message -> message instanceof SendMessage);
        run(this::processWrite).when(message -> message instanceof ValueMessage);
    }

    private void write(Object message, AbstractActor.ActorContext context) {
        numberOfRequests++;
        writeRequests.put(numberOfRequests, new WriteRequest(numberOfRequests, (WriteMessage) message));
        address.forEach(ref -> ref.tell(new SendMessage(numberOfRequests), self()));
    }

    private void sendToSender(Object message, AbstractActor.ActorContext context) {
        SendMessage sendMessage = (SendMessage) message;
        context.sender().tell(new ValueMessage(timeStamp, value, sendMessage.requestNumber()), self());
    }

    private void processWrite(Object message, AbstractActor.ActorContext context) {
        ValueMessage valueMessage = (ValueMessage) message;

        if (writeRequests.containsKey(valueMessage.requestNumber())) {
            WriteRequest writeRequest = writeRequests.get(valueMessage.requestNumber());
            writeRequest.values.add(valueMessage);

            if (writeRequests.values().size() + 1 > address.size() / 2) {
                ValueMessage newValue = writeRequest.getGreater();
                timeStamp = newValue.timeStamp() + 1;
                address.forEach(ref -> ref.tell(new UpdateMessage(timeStamp, writeRequest.value), self()));
                writeRequests.remove(valueMessage.requestNumber());
            }
        }
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

    class WriteRequest {
        public final int requestNumber;
        public final int value;
        public final List<ValueMessage> values = new LinkedList<>();

        WriteRequest(int requestNumber, WriteMessage message) {
            this.requestNumber = requestNumber;
            this.value = message.value();
        }

        public ValueMessage getGreater() {
            values.sort(Comparator.comparingInt(ValueMessage::timeStamp));
            return values.getLast();
        }
    }
}