package org.telecom.slr.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.telecom.slr.actor.helper.IdentityGenerator;
import org.telecom.slr.actor.messages.*;

import java.util.*;

public class Process extends Actor {
    private States state;
    private final List<ActorRef> address = new LinkedList<>();
    private final Map<String, WriteRequest> requests = new HashMap<>();
    private final Queue<AkkaMessage> mailbox = new LinkedList<>();

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
        run((message, context) -> mailbox.add(new AkkaMessage(message, context)))
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

        //handling reading request
        run(this::startReading).when(message -> message instanceof ReadMessage);
    }

    private void startWriting(Object message, AbstractActor.ActorContext context) {
        startWriting(message, context.sender());
    }

    private void startWriting(Object message, ActorRef sender) {
        state = States.WRITING;
        WriteMessage writeMessage = (WriteMessage) message;
        numberOfRequests++;
        String requestId = String.format("%d%d", id, numberOfRequests);
        requests.put(requestId, new WriteRequest(requestId, sender, writeMessage.value()));
        address.forEach(ref -> ref.tell(new SendMessage(requestId), self()));
    }

    private void getValue(Object message, AbstractActor.ActorContext context) {
        SendMessage sendMessage = (SendMessage) message;
        context.sender().tell(new ValueMessage(timeStamp, value, sendMessage.requestId()), self());
    }

    private void handleProcessValue(Object message, AbstractActor.ActorContext context) {
        ValueMessage valueMessage = (ValueMessage) message;
        String requestId = valueMessage.requestId();

        if (requests.containsKey(requestId) && !requests.get(requestId).isFinished(address.size())) {
            WriteRequest request = requests.get(requestId);
            request.add(valueMessage);
            
            if(request.isFinished(address.size())){
                ValueMessage greater = request.getGreater();
                address.forEach(ref -> ref.tell(new UpdateMessage(requestId, greater.timeStamp(), greater.value()), self()));
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

        if (requests.containsKey(requestId)) {
            WriteRequest request = requests.get(requestId);
            request.add(writtenValue);
            if (request.IsAllWrittenInTheMajority(address.size())) {
                request.tellAboutTheEnd(self());
                requests.remove(requestId);
                verifyMailbox();
            }
        }
    }

    private void startReading(Object message, AbstractActor.ActorContext context) {
        state = States.READING;
        numberOfRequests++;
        String requestId = String.format("%d%d",id,numberOfRequests);
        requests.put(requestId, new ReadRequest(requestId, context.sender(), -1));
        address.forEach(ref -> ref.tell(new SendMessage(requestId), self()));
    }

    private void verifyMailbox() {
        state = States.WAITING;
        AkkaMessage request = mailbox.poll();
        if (request != null) {
            startWriting(request.message, request.sender);
        }
    }

    class WriteRequest {
        protected final ActorRef requester;
        protected int value;
        protected final String requestId;

        protected final List<ValueMessage> values = new LinkedList<>();
        protected final List<WrittenValueMessage> writtenValues = new LinkedList<>();

        WriteRequest(String requestId, ActorRef requester, int value) {
            this.requestId = requestId;
            this.requester = requester;
            this.value = value;
        }

        public void tellAboutTheEnd(ActorRef self) {
            requester.tell(new WriteIssued(requestId, timeStamp, value), self);
        }

        public void add(ValueMessage message) {
            this.values.add(message);
        }

        public void add(WrittenValueMessage writtenValue) {
            writtenValues.add(writtenValue);
        }

        public boolean isFinished(int numberOfProcess) {
            return this.values.size() > numberOfProcess/2;
        }

        public boolean IsAllWrittenInTheMajority(int size) {
            return this.writtenValues.size() > size/2;
        }

        public ValueMessage getGreater() {
            values.sort(Comparator.comparingInt(ValueMessage::timeStamp));
            return new ValueMessage(
                    values.getLast().timeStamp() + 1,
                    this.value,
                    values.getLast().requestId());
        }
    }

    class ReadRequest extends WriteRequest {
        ReadRequest(String requestId, ActorRef requester, int value) {
            super(requestId, requester, value);
        }

        @Override
        public ValueMessage getGreater() {
            values.sort(Comparator.comparingInt(ValueMessage::timeStamp));
            return values.getLast();
        }

        @Override
        public void tellAboutTheEnd(ActorRef self) {
            ValueMessage value = getGreater();
            requester.tell(new ReadIssued(value.requestId(), value.timeStamp(), value.value()), self);
        }
    }


    class AkkaMessage {
        public final Object message;
        public final ActorRef sender;

        AkkaMessage(Object message, ActorContext context) {
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