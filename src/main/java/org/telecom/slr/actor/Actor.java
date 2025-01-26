package org.telecom.slr.actor;

import akka.actor.UntypedAbstractActor;
import org.telecom.slr.actor.handler.Action;
import org.telecom.slr.actor.handler.Handler;
import org.telecom.slr.actor.handler.MessageHandler;
import org.telecom.slr.actor.handler.VerifierBuilder;

import java.util.ArrayList;
import java.util.List;

public class Actor extends UntypedAbstractActor {
    private final List<Handler> handlers;

    public Actor() {
        this.handlers = new ArrayList<>();
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        messageProcessor(message, getContext());
    }

    public void messageProcessor(Object message, ActorContext context) {
        for (Handler handler : handlers) {
            if (handler.when(message)) {
                try {
                    handler.run(message, context);
                } catch (Exception e) {
                    System.err.println(context);
                }
            }
        }
    }

    public VerifierBuilder run(Action action) {
        MessageHandler builder = new MessageHandler();
        handlers.add(builder);
        return builder.setAction(action);
    }
}
