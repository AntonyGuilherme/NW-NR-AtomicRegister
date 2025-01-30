package org.telecom.slr.actor.messages;

public record WriteIssued(String requestId, int node, int timeStamp, int value, Long start, Long end) {}
