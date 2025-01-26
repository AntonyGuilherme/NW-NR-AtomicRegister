package org.telecom.slr.actor.messages;

public record UpdateMessage(String requestId, int timeStamp, int value) {
}
