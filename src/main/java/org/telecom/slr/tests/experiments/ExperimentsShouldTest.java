package org.telecom.slr.tests.experiments;

import org.junit.Assert;
import org.junit.Test;
import org.telecom.slr.actor.messages.ReadIssued;
import org.telecom.slr.actor.messages.WriteIssued;
import org.telecom.slr.experiments.*;
import org.telecom.slr.tests.actor.ActorListener;

import java.io.IOException;
import java.util.Comparator;

public class ExperimentsShouldTest {
    private final ExperimentDataCollector collector = new ExperimentDataCollector();

    @Test
    public void readTheParametersFromAnExperimentFile() throws IOException {
        ExperimentsModel model = collector.collectFromJsonFile("experiments_tests.json");

        Assert.assertArrayEquals(new Integer[]{7, 10}, model.numbersOfProcess.toArray());
        Assert.assertArrayEquals(new Integer[]{3, 3}, model.numbersOfDeactivatedProcess.toArray());
        Assert.assertArrayEquals(new Integer[]{3, 3}, model.numbersOfMessages.toArray());
    }

    @Test
    public void informTheInformationIssuedOfEveryExperiment() throws IOException, InterruptedException {
        ExperimentsModel model = collector.collectFromJsonFile("experiments_tests.json");

        Experiment experiment = new Experiment(model);

        experiment.run();
        Thread.sleep(100);

        ExperimentResultCollectorActor.experiments.getFirst()
                .writesIssued.sort(Comparator.comparingLong(WriteIssued::start));
        ExperimentResultCollectorActor.experiments.getFirst()
                .readsIssued.sort(Comparator.comparingLong(ReadIssued::start));

        ExperimentResultCollectorActor.experiments.getFirst()
                .writesIssued.sort(Comparator.comparingLong(WriteIssued::end));
        ExperimentResultCollectorActor.experiments.getFirst()
                .readsIssued.sort(Comparator.comparingLong(ReadIssued::end));

        Assert.assertEquals(3, ExperimentResultCollectorActor.experiments.getFirst().numberOfMessages);
        Assert.assertEquals(7, ExperimentResultCollectorActor.experiments.getFirst().numberOfProcess);
        Assert.assertEquals(12, ExperimentResultCollectorActor.experiments.getFirst().readsIssued.size());
        Assert.assertEquals(12, ExperimentResultCollectorActor.experiments.getFirst().writesIssued.size());

        Assert.assertEquals(3, ExperimentResultCollectorActor.experiments.getLast().numberOfMessages);
        Assert.assertEquals(10, ExperimentResultCollectorActor.experiments.getLast().numberOfProcess);
        Assert.assertEquals(21, ExperimentResultCollectorActor.experiments.getLast().readsIssued.size());
        Assert.assertEquals(21, ExperimentResultCollectorActor.experiments.getLast().writesIssued.size());
    }

    @Test
    public void informTheStartAndEndOfEveryExperiment() throws IOException, InterruptedException {
        ExperimentsModel model = collector.collectFromJsonFile("experiments_tests.json");

        Experiment experiment = new Experiment(model);

        experiment.run();
        Thread.sleep(100);

        Long firstExperimentStartTime = getExperimentStartTime(ExperimentResultCollectorActor.experiments.getFirst());
        Long firstExperimentEndTime = getExperimentEndTime(ExperimentResultCollectorActor.experiments.getFirst());
        Long latencyFirstExperiment = firstExperimentEndTime - firstExperimentStartTime;

        Long secondExperimentStartTime = getExperimentStartTime(ExperimentResultCollectorActor.experiments.getLast());
        Long secondExperimentEndTime = getExperimentEndTime(ExperimentResultCollectorActor.experiments.getLast());
        Long latencySecondExperiment = secondExperimentEndTime - secondExperimentStartTime;

        Assert.assertEquals(latencyFirstExperiment, ExperimentResultCollectorActor.experiments.getFirst().getLatency());
        Assert.assertEquals(latencySecondExperiment, ExperimentResultCollectorActor.experiments.getLast().getLatency());
    }

    public Long getExperimentStartTime(ExperimentResultModel model) {
        model.writesIssued.sort(Comparator.comparingLong(WriteIssued::start));
        model.readsIssued.sort(Comparator.comparingLong(ReadIssued::start));

        Long lessStartWriting = model.writesIssued.getFirst().start();
        Long lessStartReading = model.readsIssued.getFirst().start();

        return lessStartReading < lessStartWriting ? lessStartReading : lessStartWriting;
    }

    public Long getExperimentEndTime(ExperimentResultModel model) {
        model.writesIssued.sort(Comparator.comparingLong(WriteIssued::end));
        model.readsIssued.sort(Comparator.comparingLong(ReadIssued::end));

        Long greaterEndWriting = model.writesIssued.getLast().end();
        Long greaterEndReading = model.readsIssued.getLast().end();

        return greaterEndReading > greaterEndWriting ? greaterEndReading : greaterEndWriting;
    }
}
