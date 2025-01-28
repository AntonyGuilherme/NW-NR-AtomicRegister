package org.telecom.slr.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.telecom.slr.actor.helper.IdentityGenerator;
import org.telecom.slr.actor.messages.*;
import org.telecom.slr.actor.requests.ProcessRequest;
import org.telecom.slr.actor.requests.ReadRequest;
import org.telecom.slr.actor.requests.WriteRequest;

import java.util.*;

public class Process extends Actor {
    private States state;
    private final List<ActorRef> address = new LinkedList<>();
    private final Map<String, ProcessRequest> requests = new HashMap<>();
    private final Queue<AkkaMessage> mailbox = new LinkedList<>();

    private final int id;
    private int numberOfRequests = 0;
    private int timeStamp = 0;
    private int value = 0;

    public Process() {
        this.id = IdentityGenerator.generate();
        address.add(self());
        state = States.WAITING;

        //run(this::log);
        run(this::getRef).when(message -> message instanceof ActorRef);

        //deactivate Process
        run((message, context) -> state = States.DEACTIVATED).when(message -> message instanceof Deactivate);

        //busy behavior
        run((message, context) -> mailbox.add(new AkkaMessage(message, context, States.WRITING)))
                .when(message -> message instanceof WriteMessage && state != States.WAITING);
        run((message, context) -> mailbox.add(new AkkaMessage(message, context, States.READING)))
                .when(message -> message instanceof ReadMessage && state != States.WAITING);

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
        run(this::startReading).when(message -> message instanceof ReadMessage && state == States.WAITING);
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (state != States.DEACTIVATED) {
            super.onReceive(message);
        }
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
            ProcessRequest request = requests.get(requestId);
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
            ProcessRequest request = requests.get(requestId);
            request.add(writtenValue);
            if (request.IsAllWrittenInTheMajority(address.size())) {
                request.tellAboutTheEnd(self());
                requests.remove(requestId);
                verifyMailbox();
            }
        }
    }

    private void startReading(Object message, AbstractActor.ActorContext context) {
        startReading(message, context.sender());
    }

    private void startReading(Object message, ActorRef sender) {
        state = States.READING;
        numberOfRequests++;
        String requestId = String.format("%d%d",id,numberOfRequests);
        requests.put(requestId, new ReadRequest(requestId, sender));
        address.forEach(ref -> ref.tell(new SendMessage(requestId), self()));
    }

    private void verifyMailbox() {
        state = States.WAITING;
        AkkaMessage request = mailbox.poll();
        if (request != null) {
            if (request.status == States.WRITING)
                startWriting(request.message, request.sender);
            if (request.status == States.READING)
                startReading(request.message, request.sender);
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