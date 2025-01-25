package org.telecom.slr.actor.handler;

import akka.actor.AbstractActor;

@FunctionalInterface
public interface Action {
    void run(Object message, AbstractActor.ActorContext context);
}
