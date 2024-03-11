package org.vivalink.handler;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQSEventsFailHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Override
    public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
        List<SQSBatchResponse.BatchItemFailure> batchItemFailures = new ArrayList<>();
        LambdaLogger logger = context.getLogger();
        logger.log("SQS EVENT Consume Start!");
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        if (records != null && !records.isEmpty()) {
            logger.log("SQS EVENT Msg Count: " + records.size());
            records.forEach(sqsMessage -> {
                String body = sqsMessage.getBody();
                logger.log("SQSMessage Body: " + body);
                String messageId = sqsMessage.getMessageId();
                try {
                    Map<String, String> map = OBJECT_MAPPER.readValue(body, Map.class);
                    String action = map.get("action");
                    if("err".equalsIgnoreCase(action)){
                        throw new RuntimeException();
                    }
                } catch (Exception e) {
                    logger.log(e.getMessage());
                    // Add failed message identifier to the batchItemFailures list
                    batchItemFailures.add(new SQSBatchResponse.BatchItemFailure(messageId));
                }
            });
        }
        return new SQSBatchResponse(batchItemFailures);
    }
}
