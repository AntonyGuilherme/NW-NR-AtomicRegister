package org.telecom.slr;

import org.telecom.slr.experiments.Experiment;
import org.telecom.slr.experiments.ExperimentDataCollector;
import org.telecom.slr.experiments.ExperimentsModel;

public class Main {
    public static void main(String[] args) {
        ExperimentDataCollector collector = new ExperimentDataCollector();
        ExperimentsModel model = collector.collectFromJsonFile("experiments.json");
        Experiment experiment = new Experiment(model);

        experiment.run();
    }
}