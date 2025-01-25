package org.telecom.slr.tests.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.telecom.slr.actor.Node;
import org.telecom.slr.actor.messages.Send;
import org.telecom.slr.actor.messages.WriteMessage;

public class ActorShouldTest {
    private ActorSystem system;
    private ActorRef listener;
    private ActorRef node;

    @Before
    public void setUp() {
        this.system = ActorSystem.create("actorSystem");
        this.listener = system.actorOf(Props.create(ActorListener.class, ActorListener::new), "listener");
        this.node = system.actorOf(Props.create(Node.class, Node::new), "node");
    }

    @Test
    public void onWritingRequestForTheValueForAllKnownNodes() throws InterruptedException {
        this.node.tell(this.listener, ActorRef.noSender());

        this.node.tell(new WriteMessage(10), this.listener);
        Thread.sleep(100);

        Assert.assertEquals(1, ActorListener.messages.size());
        Assert.assertTrue(ActorListener.messages.stream().allMatch(m -> ((Send) m).numberOfRequests() == 1));
    }

    @After
    public void tearDown() {
        this.system.terminate();
    }
}
