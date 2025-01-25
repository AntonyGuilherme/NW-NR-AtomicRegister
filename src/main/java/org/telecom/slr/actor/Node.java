package org.telecom.slr.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.telecom.slr.actor.messages.*;

import java.util.*;

public class Node extends Actor {
    private final List<ActorRef> address = new LinkedList<>();
    private final Map<Integer, WriteRequest> writeRequests = new HashMap<>();

    private int numberOfRequests = 0;
    private int timeStamp = -1;
    private int value = -1;

    public Node() {
        run(this::log);
        run(this::getRef).when(message -> message instanceof ActorRef);

        run(this::write).when(message -> message instanceof WriteMessage);
        run(this::sendToSender).when(message -> message instanceof SendMessage);
        run(this::processWrite).when(message -> message instanceof ValueMessage);
        run(this::overwrite).when(message -> message instanceof UpdateMessage);
        run(this::confirmOverwrite).when(message -> message instanceof WrittenValueMessage);
    }

    private void write(Object message, AbstractActor.ActorContext context) {
        numberOfRequests++;
        writeRequests.put(numberOfRequests, new WriteRequest(context.sender(), numberOfRequests, (WriteMessage) message));
        address.forEach(ref -> ref.tell(new SendMessage(numberOfRequests), self()));
    }

    private void sendToSender(Object message, AbstractActor.ActorContext context) {
        SendMessage sendMessage = (SendMessage) message;
        context.sender().tell(new ValueMessage(timeStamp, value, sendMessage.requestNumber()), self());
    }

    private void processWrite(Object message, AbstractActor.ActorContext context) {
        ValueMessage valueMessage = (ValueMessage) message;
        int requestNumber = valueMessage.requestNumber();

        if (writeRequests.containsKey(valueMessage.requestNumber()) && 
                !writeRequests.get(valueMessage.requestNumber()).allNecessaryValuesInformed) {

            WriteRequest writeRequest = writeRequests.get(valueMessage.requestNumber());
            writeRequest.values.add(valueMessage);

            if (writeRequest.values.size() > address.size() / 2) {
                ValueMessage newValue = writeRequest.getGreater();
                timeStamp = newValue.timeStamp() + 1;
                address.forEach(ref -> ref.tell(new UpdateMessage(requestNumber, timeStamp, writeRequest.value), self()));
                writeRequest.allNecessaryWereValuesInformed(timeStamp);
            }
        }
    }

    private void overwrite(Object message, AbstractActor.ActorContext context) {
        UpdateMessage updateMessage = (UpdateMessage) message;
        int timeStamp = updateMessage.timeStamp();
        int value = updateMessage.value();

        if (timeStamp > this.timeStamp || (this.timeStamp == timeStamp && this.value == value)) {
            this.value = value;
            this.timeStamp = timeStamp;
        }

        context.sender().tell(new WrittenValueMessage(updateMessage.requestNumber(), this.timeStamp, this.value), self());
    }

    private void confirmOverwrite(Object message, AbstractActor.ActorContext context) {
        WrittenValueMessage writtenValue = (WrittenValueMessage) message;
        int requestNumber = writtenValue.requestNumber();
        int timeStamp = writtenValue.timeStamp();
        int value = writtenValue.value();

        if (writeRequests.containsKey(requestNumber)) {
            WriteRequest writeRequest = writeRequests.get(requestNumber);

            if (writeRequest.timeStamp == timeStamp && writeRequest.value == value) {
                writeRequest.writtenValues.add(writtenValue);

                if (writeRequest.writtenValues.size() > address.size() / 2) {
                    writeRequest.requester.tell(new WriteIssued(requestNumber, timeStamp, value), self());
                    writeRequests.remove(requestNumber);
                }
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
        public final ActorRef requester;
        public final int requestNumber;
        public int timeStamp;
        public final int value;

        public final List<ValueMessage> values = new LinkedList<>();
        public final List<WrittenValueMessage> writtenValues = new LinkedList<>();
        public boolean allNecessaryValuesInformed = false;

        WriteRequest(ActorRef requester, int requestNumber, WriteMessage message) {
            this.requester = requester;
            this.requestNumber = requestNumber;
            this.value = message.value();
        }

        public ValueMessage getGreater() {
            values.sort(Comparator.comparingInt(ValueMessage::timeStamp));
            return values.getLast();
        }

        public void allNecessaryWereValuesInformed(int timeStamp) {
            this.allNecessaryValuesInformed = true;
            this.timeStamp = timeStamp;

        }
    }
}