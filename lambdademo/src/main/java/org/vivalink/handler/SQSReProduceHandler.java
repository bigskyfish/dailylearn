package org.vivalink.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.vivalink.pools.SQSClientPool;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQSReProduceHandler  implements RequestHandler<SQSEvent, List<String>> {


    public static  final String QUEUE_URL = "https://sqs.ap-northeast-1.amazonaws.com/471112901071/test-demo";
    @Override
    public List<String> handleRequest(SQSEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        List<SQSEvent.SQSMessage> records = event.getRecords();
        if (records != null && !records.isEmpty()) {
            logger.log("SQS EVENT Msg Count: " + records.size());
            records.forEach(sqsMessage -> {
                Map<String, SQSEvent.MessageAttribute> messageAttributes = sqsMessage.getMessageAttributes();
                SQSEvent.MessageAttribute triedAttribute = messageAttributes.get("tried");
                SQSEvent.MessageAttribute maxTried = messageAttributes.get("max_tried");
                int tried = Integer.parseInt(triedAttribute.getStringValue());
                int max = Integer.parseInt(maxTried.getStringValue());
                logger.log("max_tried is : " + max + " ; tried is : " + tried + " ; body is : " + sqsMessage.getBody() + " ; messageAttributes is : " + messageAttributes + " ; messageAttributes.size() is : " + messageAttributes.size());
                if (tried < max){
                    if (tried == 10){
                        throw new RuntimeException();
                    }
                    tried++;
                    Map<String, MessageAttributeValue> sendAttributes = new HashMap<>();
                    sendAttributes.put("tried", MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(String.valueOf(tried))
                            .build());
                    sendAttributes.put("max_tried", MessageAttributeValue.builder()
                            .dataType("String")
                            .stringValue(String.valueOf(max))
                            .build());
                    String body = sqsMessage.getBody();
                    SQSClientPool.sendMsg(QUEUE_URL, body, sendAttributes, 0);
                }
            });
            return records.stream().map(SQSEvent.SQSMessage::getBody).toList();
        }
        return null;
    }
}
