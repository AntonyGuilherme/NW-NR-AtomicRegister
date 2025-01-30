package org.telecom.slr.actor.requests;

import akka.actor.ActorRef;
import org.telecom.slr.actor.messages.ReadIssued;
import org.telecom.slr.actor.messages.ValueMessage;

import java.util.Comparator;

public class ReadRequest extends ProcessRequest {
    public ReadRequest(String requestId, ActorRef requester) {
        super(requestId, requester);
    }

    @Override
    public ValueMessage getGreater() {
        values.sort(Comparator.comparingInt(ValueMessage::timeStamp));
        return values.getLast();
    }

    @Override
    public void tellAboutTheEnd(ActorRef self) {
        ValueMessage value = getGreater();
        requester.tell(new ReadIssued(value.requestId(),
                value.timeStamp(), value.value(), start,
                System.nanoTime()), self);
    }
}
