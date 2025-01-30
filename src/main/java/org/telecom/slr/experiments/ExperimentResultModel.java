package org.telecom.slr.experiments;

import org.telecom.slr.actor.messages.ReadIssued;
import org.telecom.slr.actor.messages.WriteIssued;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ExperimentResultModel {
    public final int numberOfProcess;
    public final int numberOfMessages;
    public final int numberOfFaultyProcesses;
    public final Long experimentStartTime;
    public final List<WriteIssued> writesIssued = new LinkedList<>();
    public final List<ReadIssued> readsIssued = new LinkedList<>();

    public ExperimentResultModel(int numberOfProcess, int numberOfMessages, int numberOfFaultyProcesses) {
        this.numberOfProcess = numberOfProcess;
        this.numberOfMessages = numberOfMessages;
        this.numberOfFaultyProcesses = numberOfFaultyProcesses;
        this.experimentStartTime = System.nanoTime();
    }

    public void addWriteIssued(WriteIssued writeIssued) {
        writesIssued.add(writeIssued);
    }

    public void addReadIssued(ReadIssued readIssued) {
        readsIssued.add(readIssued);
    }

    public Long getLatency() {
        writesIssued.sort(Comparator.comparingLong(WriteIssued::start));
        readsIssued.sort(Comparator.comparingLong(ReadIssued::start));

        Long lessStartWriting = writesIssued.getFirst().start();
        Long lessStartReading = readsIssued.getFirst().start();

        Long start = lessStartReading < lessStartWriting ? lessStartReading : lessStartWriting;

        writesIssued.sort(Comparator.comparingLong(WriteIssued::end));
        readsIssued.sort(Comparator.comparingLong(ReadIssued::end));

        Long greaterEndWriting = writesIssued.getLast().end();
        Long greaterEndReading = readsIssued.getLast().end();

        Long end = greaterEndReading > greaterEndWriting ? greaterEndReading : greaterEndWriting;

        return end - start;
    }
}