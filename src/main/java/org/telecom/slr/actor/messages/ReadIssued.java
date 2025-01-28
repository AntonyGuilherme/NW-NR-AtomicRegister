package org.telecom.slr.actor.messages;

public record ReadIssued(String requestId, int timestamp, int value, Long start, Long end) {

}
