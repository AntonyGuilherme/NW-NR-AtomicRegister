package org.telecom.slr.experiments;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.telecom.slr.actor.Actor;
import org.telecom.slr.actor.messages.ReadIssued;
import org.telecom.slr.actor.messages.WriteIssued;
import java.util.LinkedList;
import java.util.List;

public class ExperimentResultCollectorActor extends Actor {
    public static List<ExperimentResultModel> experiments = new LinkedList<>();
    private LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    public ExperimentResultCollectorActor() {
        run(this::addExperimentWriteIssuedResult).when(message -> message instanceof WriteIssued);
        run(this::addExperimentReadIssuedResult).when(message -> message instanceof ReadIssued);

       run(this::log).when(message -> message instanceof WriteIssued);
       run(this::log).when(message -> message instanceof ReadIssued);
    }

    public static void fromNowOnCollect(int numberOfProcess, int numberOfMessage, int numberOfFaultyProcesses) {
        ExperimentResultModel model = new ExperimentResultModel(numberOfProcess, numberOfMessage, numberOfFaultyProcesses);
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

    private void log(Object message, AbstractActor.ActorContext context) {
        String from = context.sender().path().name();
        String to = context.self().path().name();

        logger.info(String.format("from %s to %s : %s", from, to, message));
    }
}
