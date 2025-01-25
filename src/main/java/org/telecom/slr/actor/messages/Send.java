package org.telecom.slr.actor.messages;

import akka.actor.ActorRef;

public record Send(int numberOfRequests) {}
