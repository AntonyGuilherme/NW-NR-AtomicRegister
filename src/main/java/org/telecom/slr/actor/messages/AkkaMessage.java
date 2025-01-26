package org.telecom.slr.actor.messages;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import org.telecom.slr.actor.States;

public class AkkaMessage {
    public final Object message;
    public final ActorRef sender;
    public final States status;

    public AkkaMessage(Object message, AbstractActor.ActorContext context, States status) {
        this.message = message;
        this.sender = context.sender();
        this.status = status;
    }
}
