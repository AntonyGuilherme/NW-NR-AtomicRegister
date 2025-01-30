package org.telecom.slr.experiments;

import org.telecom.slr.actor.Actor;
import org.telecom.slr.actor.messages.ReadIssued;
import org.telecom.slr.actor.messages.WriteIssued;
import java.util.LinkedList;
import java.util.List;

public class ExperimentResultCollectorActor extends Actor {
    public static List<ExperimentResultModel> experiments = new LinkedList<>();

    public ExperimentResultCollectorActor() {
        run(this::addExperimentWriteIssuedResult).when(message -> message instanceof WriteIssued);
        run(this::addExperimentReadIssuedResult).when(message -> message instanceof ReadIssued);
    }

    public static void fromNowOnCollect(int numberOfProcess, int numberOfMessage) {
        ExperimentResultModel model = new ExperimentResultModel(numberOfProcess, numberOfMessage);
        experiments.add(model);
    }

    private void addExperimentReadIssuedResult(Object message, ActorContext context) {
        ReadIssued readIssued = (ReadIssued) message;
        experiments.getLast().addReadIssued(readIssued);
    }

    private void addExperimentWriteIssuedResult(Object message, ActorContext context) {
        WriteIssued writeIssued = (WriteIssued) message;
        experiments.getLast().addWriteIssued(writeIssued);
    }
}
