package org.telecom.slr.actor.messages;

public record ValueMessage(int timeStamp, int value, int requestNumber) {
}
