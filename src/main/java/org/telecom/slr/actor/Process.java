package org.telecom.slr.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.telecom.slr.actor.helper.IdentityGenerator;
import org.telecom.slr.actor.messages.*;

import java.util.*;

public class Process extends Actor {
    private States state;
    private final List<ActorRef> address = new LinkedList<>();
    private final Map<String, WriteRequest> writeRequests = new HashMap<>();
    private final Queue<Request> mailbox = new LinkedList<>();

    private final int id;
    private int numberOfRequests = 0;
    private int timeStamp = -0;
    private int value = 0;

    public Process() {
        this.id = IdentityGenerator.generate();
        address.add(self());
        state = States.WAITING;

        run(this::log);
        run(this::getRef).when(message -> message instanceof ActorRef);

        //busy behavior
        run((message, context) -> mailbox.add(new Request(message, context)))
                .when(message -> message instanceof WriteMessage && state != States.WAITING);

        // executes if the process is not busy
        run(this::startWriting).when(message -> message instanceof WriteMessage && state == States.WAITING);

        // receiving the values and timesStamps from the others knows process
        run(this::getValue).when(message -> message instanceof SendMessage);

        // handle the values while a writing is happening
        run(this::handleProcessValue).when(message -> message instanceof ValueMessage);

        // setting the value to the actual local value and register
        run(this::setValue).when(message -> message instanceof UpdateMessage);

        // handling confirmation of the process value.
        run(this::handleProcessConfirmationOfReceivingValue).when(message -> message instanceof WrittenValueMessage);
    }

    private void startWriting(Object message, AbstractActor.ActorContext context) {
        startWriting(message, context.sender());
    }

    private void startWriting(Object message, ActorRef sender) {
        state = States.WRITING;
        numberOfRequests++;
        String requestId = String.format("%d%d",id,numberOfRequests);
        writeRequests.put(requestId, new WriteRequest(sender, numberOfRequests, (WriteMessage) message));
        address.forEach(ref -> ref.tell(new SendMessage(requestId), self()));
    }

    private void getValue(Object message, AbstractActor.ActorContext context) {
        SendMessage sendMessage = (SendMessage) message;
        context.sender().tell(new ValueMessage(timeStamp, value, sendMessage.requestId()), self());
    }

    private void handleProcessValue(Object message, AbstractActor.ActorContext context) {
        ValueMessage valueMessage = (ValueMessage) message;
        String requestId = valueMessage.requestId();

        if (writeRequests.containsKey(requestId) &&
                !writeRequests.get(requestId).allNecessaryValuesInformed) {

            WriteRequest writeRequest = writeRequests.get(requestId);
            writeRequest.values.add(valueMessage);

            if (writeRequest.values.size() > address.size() / 2) {
                ValueMessage newValue = writeRequest.getGreater();
                writeRequest.allNecessaryWereValuesInformed(newValue.timeStamp() + 1);
                address.forEach(ref -> ref.tell(new UpdateMessage(requestId, writeRequest.timeStamp, writeRequest.value), self()));
            }
        }
    }

    private void setValue(Object message, AbstractActor.ActorContext context) {
        UpdateMessage updateMessage = (UpdateMessage) message;
        int timeStamp = updateMessage.timeStamp();
        int value = updateMessage.value();

        if (timeStamp > this.timeStamp || (this.timeStamp == timeStamp && this.value == value)) {
            this.value = value;
            this.timeStamp = timeStamp;
        }

        context.sender().tell(new WrittenValueMessage(updateMessage.requestId(), timeStamp, value), self());
    }

    private void handleProcessConfirmationOfReceivingValue(Object message, AbstractActor.ActorContext context) {
        WrittenValueMessage writtenValue = (WrittenValueMessage) message;
        String requestId = writtenValue.requestId();
        int timeStamp = writtenValue.timeStamp();
        int value = writtenValue.value();

        if (writeRequests.containsKey(requestId)) {
            WriteRequest writeRequest = writeRequests.get(requestId);

            if (writeRequest.timeStamp == timeStamp && writeRequest.value == value) {
                writeRequest.writtenValues.add(writtenValue);

                if (writeRequest.writtenValues.size() > address.size() / 2) {
                    writeRequest.requester.tell(new WriteIssued(requestId, timeStamp, value), self());
                    writeRequests.remove(requestId);
                    verifyMailbox();
                }
            }
        }
    }

    private void verifyMailbox() {
        state = States.WAITING;
        Request request = mailbox.poll();
        if (request != null) {
            startWriting(request.message, request.sender);
        }
    }

    class WriteRequest {
        public final ActorRef requester;
        public int timeStamp;
        public final int value;

        public final List<ValueMessage> values = new LinkedList<>();
        public final List<WrittenValueMessage> writtenValues = new LinkedList<>();
        public boolean allNecessaryValuesInformed = false;

        WriteRequest(ActorRef requester, int requestNumber, WriteMessage message) {
            this.requester = requester;
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

    class Request {
        public final Object message;
        public final ActorRef sender;

        Request(Object message, ActorContext context) {
            this.message = message;
            this.sender = context.sender();
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
}