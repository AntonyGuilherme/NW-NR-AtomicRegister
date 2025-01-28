package org.telecom.slr.tests.experiments;

import org.junit.Assert;
import org.junit.Test;
import org.telecom.slr.actor.messages.WriteIssued;
import org.telecom.slr.experiments.Experiment;
import org.telecom.slr.experiments.ExperimentDataCollector;
import org.telecom.slr.experiments.ExperimentsModel;
import org.telecom.slr.tests.actor.ActorListener;

import java.io.IOException;

public class ExperimentsShouldTest {
    private final ExperimentDataCollector collector = new ExperimentDataCollector();

    @Test
    public void readTheParametersFromAnExperimentFile() throws IOException {
        ExperimentsModel model = collector.collectFromJsonFile("experiments.json");

        Assert.assertArrayEquals(new Integer[]{7}, model.numbersOfProcess.toArray());
        Assert.assertArrayEquals(new Integer[]{3}, model.numbersOfDeactivatedProcess.toArray());
        Assert.assertArrayEquals(new Integer[]{3}, model.numbersOfMessages.toArray());
    }

    @Test
    public void informTheStartAndEndOfEveryExperiment() throws IOException, InterruptedException {
        ExperimentsModel model = collector.collectFromJsonFile("experiments.json");

        Experiment experiment = new Experiment(model);

        experiment.run();
        Thread.sleep(100);

        Assert.assertEquals(12, ActorListener.messages.stream().filter(m -> m instanceof WriteIssued).count());
    }
}
