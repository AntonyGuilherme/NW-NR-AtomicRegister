package org.telecom.slr.actor.handler;

public class VerifierBuilder {
    private final MessageHandler handler;

    public VerifierBuilder(MessageHandler handler) {
        this.handler = handler;
    }

    public void when(Verifier verifier) {
        this.handler.setVerifier(verifier);
    }
}
