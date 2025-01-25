package org.telecom.slr.actor.messages;

public record WrittenValueMessage(int requestNumber, int timeStamp, int value) {}