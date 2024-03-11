package org.vivalink.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class SQSEventsHandler implements RequestHandler<SQSEvent, List<String>> {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Override
    public List<String> handleRequest(SQSEvent sqsEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("SQS EVENT Consume Start!");
        List<SQSEvent.SQSMessage> records = sqsEvent.getRecords();
        if (records != null && !records.isEmpty()) {
            logger.log("SQS EVENT Msg Count: " + records.size());
            records.forEach(sqsMessage -> {
                String body = sqsMessage.getBody();
                logger.log("SQSMessage Body: " + body);
                Map<String, String> map = null;
                try {
                    map = OBJECT_MAPPER.readValue(body, Map.class);
                } catch (JsonProcessingException e) {
                    logger.log(e.getMessage());
                }
                String action = map.get("action");
                if("exception".equalsIgnoreCase(action)){
                    throw new RuntimeException();
                }
            });
            return records.stream().map(SQSEvent.SQSMessage::getBody).toList();
        }
       return null;
    }
}
