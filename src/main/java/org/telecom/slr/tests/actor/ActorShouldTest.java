package org.telecom.slr.tests.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.telecom.slr.actor.Node;
import org.telecom.slr.actor.messages.*;

import java.util.LinkedList;
import java.util.List;

public class ActorShouldTest {
    private ActorSystem system;
    private ActorRef listener;
    private ActorRef node;
    private List<ActorRef> otherNodes;

    @Before
    public void setUp() throws InterruptedException {
        this.system = ActorSystem.create("actorSystem");
        this.listener = system.actorOf(Props.create(ActorListener.class, ActorListener::new), "listener");
        this.node = system.actorOf(Props.create(Node.class, Node::new), "node");
        this.otherNodes = new LinkedList<>();
        this.node.tell(this.listener, ActorRef.noSender());

        for (int i = 0; i < 2; i++) {
            otherNodes.add(system.actorOf(Props.create(Node.class, Node::new), "node"+i));
            this.node.tell(otherNodes.get(i), ActorRef.noSender());
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                if (i != j) {
                    otherNodes.get(i).tell(otherNodes.get(j), ActorRef.noSender());
                }
            }

            otherNodes.get(i).tell(this.node, ActorRef.noSender());
        }

        Thread.sleep(100);
    }

    @Test
    public void onWritingRequestForTheValueForAllKnownNodes() throws InterruptedException {
        this.node.tell(new WriteMessage(10), this.listener);
        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream().anyMatch(m -> ((SendMessage) m).requestNumber() == 1));
    }

    @Test
    public void onWritingInformTheNewProposedValueForAllKnownNodes() throws InterruptedException {
        this.node.tell(new WriteMessage(10), this.listener);

        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream()
                .anyMatch(m -> m instanceof UpdateMessage && ((UpdateMessage) m).value() == 10));
    }

    @Test
    public void onWritingInformThatTheNewProposedValueWasWritten() throws InterruptedException {
        this.node.tell(new WriteMessage(10), this.listener);

        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream().filter(m -> m instanceof WriteIssued)
                .anyMatch(m -> ((WriteIssued) m).value() == 10));
        Assert.assertEquals(1,
                ActorListener.messages.stream().filter(m -> m instanceof WriteIssued).count());
    }

    @Test
    public void onWritingInformThatTheNewProposedValueWasWrittenEvenIfTwoWrittenAreHappeningConcurrently() throws InterruptedException {
        this.node.tell(new WriteMessage(10), this.listener);
        this.node.tell(new WriteMessage(5), this.listener);

        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream().filter(m -> m instanceof WriteIssued)
                .allMatch(m -> ((WriteIssued) m).timeStamp() == 0));
        Assert.assertEquals(2,
                ActorListener.messages.stream().filter(m -> m instanceof WriteIssued).count());
    }

    @Test
    public void onWritingInformThatTheNewProposedValueWasWrittenEvenIfTwoWrittenAreHappensSequentially() throws InterruptedException {
        this.node.tell(new WriteMessage(10), this.listener);
        Thread.sleep(100);

        this.node.tell(new WriteMessage(5), this.listener);
        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream().filter(m -> m instanceof WriteIssued)
                .anyMatch(m -> ((WriteIssued) m).timeStamp() == 1));
        Assert.assertTrue(ActorListener.messages.stream().filter(m -> m instanceof WriteIssued)
                .anyMatch(m -> ((WriteIssued) m).timeStamp() == 0));
        Assert.assertEquals(2,
                ActorListener.messages.stream().filter(m -> m instanceof WriteIssued).count());
    }

    @Test
    public void onWritingInformThatTheNewProposedValueWasWrittenWithTheMostRecentTimeStamp() throws InterruptedException {

        ActorRef otherNode = createAndTellOthers("other");
        Thread.sleep(100);

        otherNode.tell(new WriteMessage(10), this.listener);
        Thread.sleep(100);

        otherNode.tell(new WriteMessage(50), this.listener);
        Thread.sleep(100);

        this.node.tell(new WriteMessage(5), this.listener);
        Thread.sleep(100);

        Assert.assertTrue(ActorListener.messages.stream().filter(m -> m instanceof WriteIssued)
                .anyMatch(m -> ((WriteIssued) m).timeStamp() == 2));
        Assert.assertEquals(3,
                ActorListener.messages.stream().filter(m -> m instanceof WriteIssued).count());
    }

    @After
    public void tearDown() {
        this.system.terminate();
        ActorListener.messages.clear();
    }

    private ActorRef createAndTellOthers(String name) {
        ActorRef node = system.actorOf(Props.create(Node.class, Node::new), name);

        for (ActorRef otherNode : otherNodes) {
            node.tell(otherNode, ActorRef.noSender());
            otherNode.tell(node, ActorRef.noSender());
        }

        node.tell(this.listener, ActorRef.noSender());

        return node;
    }
}
