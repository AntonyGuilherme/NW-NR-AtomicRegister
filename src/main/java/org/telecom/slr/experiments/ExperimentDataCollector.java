package org.telecom.slr.experiments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ExperimentDataCollector {
    public ExperimentsModel collectFromJsonFile(String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(new File(fileName));

            List<Integer> numbersOfProcess = convert(jsonNode.get("numbersOfProcess").elements());
            List<Integer> numbersOfDeactivatedProcess = convert(jsonNode.get("numbersOfDeactivatedProcess").elements());
            List<Integer> numbersOfMessages = convert(jsonNode.get("numbersOfMessages").elements());

            return new ExperimentsModel(numbersOfProcess, numbersOfDeactivatedProcess, numbersOfMessages);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Integer> convert(Iterator<JsonNode> elements) {
        List<Integer> convertedElements = new LinkedList<>();

        while (elements.hasNext()) {
            convertedElements.add(elements.next().asInt());
        }

        return convertedElements;
    }
}
