package org.telecom.slr.actor.handler;

@FunctionalInterface
public interface Verifier {
    boolean when(Object message);
}
