package org.telecom.slr.actor.messages;

public record UpdateMessage(int requestNumber, int timeStamp, int value) {
}
