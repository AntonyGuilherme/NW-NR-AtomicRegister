package org.telecom.slr.actor.requests;

import akka.actor.ActorRef;
import org.telecom.slr.actor.messages.ValueMessage;
import org.telecom.slr.actor.messages.WriteIssued;

import java.util.Comparator;

public class WriteRequest extends ProcessRequest {
    protected final int value;

    public WriteRequest(String requestId, int node, ActorRef requester, int value) {
        super(requestId, requester, node);
        this.value = value;
    }

    @Override
    public void tellAboutTheEnd(ActorRef self) {
        Long end = System.nanoTime();
        requester.tell(new WriteIssued(requestId, node, getGreater().timeStamp(), value, this.start, end), self);
    }

    @Override
    public ValueMessage getGreater() {
        values.sort(Comparator.comparingInt(ValueMessage::timeStamp));
        return new ValueMessage(
                values.getLast().timeStamp() + 1,
                this.value,
                values.getLast().requestId());
    }
}
