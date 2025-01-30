package org.telecom.slr.experiments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.telecom.slr.actor.messages.ReadIssued;
import org.telecom.slr.actor.messages.WriteIssued;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExperimentResultWriter {

    public static void write(List<ExperimentResultModel> results) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode array = json.putArray("results");

        for (ExperimentResultModel result : results) {

            ObjectNode experiment = array.addObject();
            experiment.put("numberOfProcess", result.numberOfProcess);
            experiment.put("numberOfMessages", result.numberOfMessages);
            experiment.put("latency", result.getLatency());

            ArrayNode writes = experiment.putArray("writes");

            for (WriteIssued issued : result.writesIssued) {
                ObjectNode write = writes.addObject();
                write.put("requestId", issued.requestId());
                write.put("value", issued.value());
                write.put("timestamp", issued.timeStamp());
                write.put("start", issued.start());
                write.put("end", issued.start());
            }

            ArrayNode reads = experiment.putArray("reads");

            for (ReadIssued issued : result.readsIssued) {
                ObjectNode read = reads.addObject();
                read.put("requestId", issued.requestId());
                read.put("value", issued.value());
                read.put("timestamp", issued.timestamp());
                read.put("start", issued.start());
                read.put("end", issued.start());
            }
        }

        objectMapper.writeValue(new File("results.json"), json);
    }

}
