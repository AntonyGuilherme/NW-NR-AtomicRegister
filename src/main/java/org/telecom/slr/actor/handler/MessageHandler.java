package org.telecom.slr.actor.handler;

import akka.actor.AbstractActor;

public class MessageHandler implements Handler {
    private Action action;
    private Verifier verifier;

    @Override
    public void run(Object message, AbstractActor.ActorContext context) {
        this.action.run(message, context);
    }

    @Override
    public boolean when(Object message) {
        return this.action != null && (this.verifier == null || this.verifier.when(message));
    }

    public VerifierBuilder setAction(Action handler) {
        this.action = handler;
        return new VerifierBuilder(this);
    }

    protected void setVerifier(Verifier verifier) {
        this.verifier = verifier;
    }
}
