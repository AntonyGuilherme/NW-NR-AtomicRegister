package org.telecom.slr.actor.messages;

public record WrittenValueMessage(String requestId, int timeStamp, int value) {}