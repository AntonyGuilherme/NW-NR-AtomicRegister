package org.telecom.slr.tests.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.telecom.slr.actor.Node;
import org.telecom.slr.actor.messages.SendMessage;
import org.telecom.slr.actor.messages.UpdateMessage;
import org.telecom.slr.actor.messages.ValueMessage;
import org.telecom.slr.actor.messages.WriteMessage;

import java.util.LinkedList;
import java.util.List;

public class ActorShouldTest {
    private ActorSystem system;
    private ActorRef listener;
    private ActorRef node;
    private List<ActorRef> otherNodes;

    @Before
    public void setUp() {
        this.system = ActorSystem.create("actorSystem");
        this.listener = system.actorOf(Props.create(ActorListener.class, ActorListener::new), "listener");
        this.node = system.actorOf(Props.create(Node.class, Node::new), "node");
        this.otherNodes = new LinkedList<>();
        this.node.tell(this.listener, ActorRef.noSender());

        for (int i = 0; i < 2; i++) {
            this.otherNodes.add(system.actorOf(Props.create(Node.class, Node::new), "node"+i));
            this.node.tell(this.otherNodes.get(i), ActorRef.noSender());
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (i != j) {
                    this.otherNodes.get(i).tell(this.otherNodes.get(j), ActorRef.noSender());
                }
            }

            this.otherNodes.get(i).tell(this.node, ActorRef.noSender());
        }
    }

    @Test
    public void onWritingRequestForTheValueForAllKnownNodes() throws InterruptedException {
        this.node.tell(new WriteMessage(10), this.listener);
        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream().anyMatch(m -> ((SendMessage) m).requestNumber() == 1));
    }

    @Test
    public void onWritingReceiveTheNewProposedValue() throws InterruptedException {
        this.node.tell(new WriteMessage(10), this.listener);

        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream()
                .anyMatch(m -> m instanceof UpdateMessage && ((UpdateMessage) m).value() == 10));
    }

    @After
    public void tearDown() {
        this.system.terminate();
        ActorListener.messages.clear();
    }
}
