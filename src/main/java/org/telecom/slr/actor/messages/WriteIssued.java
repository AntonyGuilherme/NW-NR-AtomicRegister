package org.telecom.slr.actor.messages;

public record WriteIssued(String requestId, int timeStamp, int value, Long start, Long end) {}
