package org.telecom.slr.tests.actor;

import org.telecom.slr.actor.Actor;

import java.util.ArrayList;
import java.util.List;

public class ActorListener extends Actor {
    public static final List<Object> messages = new ArrayList<Object>();

    public ActorListener() {
        run((message, context) -> messages.add(message));
    }
}
