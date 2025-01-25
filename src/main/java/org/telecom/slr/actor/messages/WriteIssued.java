package org.telecom.slr.actor.messages;

public record WriteIssued(int requestNumber, int timeStamp, int value) {}
