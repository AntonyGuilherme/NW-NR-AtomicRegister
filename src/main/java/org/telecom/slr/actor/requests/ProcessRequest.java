package org.telecom.slr.actor.requests;

import akka.actor.ActorRef;
import org.telecom.slr.actor.messages.ValueMessage;
import org.telecom.slr.actor.messages.WrittenValueMessage;

import java.util.LinkedList;
import java.util.List;

public abstract class ProcessRequest {
    protected final ActorRef requester;
    protected final String requestId;
    protected final List<ValueMessage> values = new LinkedList<>();
    protected final List<WrittenValueMessage> writtenValues = new LinkedList<>();
    protected final Long start;
    protected final int node;

    ProcessRequest(String requestId, ActorRef requester, int node) {
        this.requestId = requestId;
        this.requester = requester;
        this.start = System.nanoTime();
        this.node = node;
    }

    public abstract void tellAboutTheEnd(ActorRef self);

    public abstract ValueMessage getGreater();

    public void add(ValueMessage message) {
        this.values.add(message);
    }

    public void add(WrittenValueMessage writtenValue) {
        writtenValues.add(writtenValue);
    }

    public boolean isFinished(int numberOfProcess) {
        return this.values.size() > numberOfProcess / 2;
    }

    public boolean IsAllWrittenInTheMajority(int size) {
        return this.writtenValues.size() > size / 2;
    }
}
