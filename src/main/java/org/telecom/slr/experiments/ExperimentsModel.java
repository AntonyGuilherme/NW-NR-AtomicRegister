package org.telecom.slr.experiments;

import java.util.List;

public class ExperimentsModel {

    public final List<Integer> numbersOfProcess;
    public final List<Integer> numbersOfDeactivatedProcess;
    public final List<Integer> numbersOfMessages;

    public ExperimentsModel(List<Integer> numbersOfProcess,
                            List<Integer> numbersOfDeactivatedProcess,
                            List<Integer> numbersOfMessages) {
        this.numbersOfProcess = numbersOfProcess;
        this.numbersOfDeactivatedProcess = numbersOfDeactivatedProcess;
        this.numbersOfMessages = numbersOfMessages;
    }
}
