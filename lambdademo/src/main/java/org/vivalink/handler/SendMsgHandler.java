package org.vivalink.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vivalink.pools.SQSClientPool;

import java.util.Map;

public class SendMsgHandler implements RequestHandler<Map<String, String>, String> {

    public static  final String QUEUE_URL = "https://sqs.ap-northeast-1.amazonaws.com/471112901071/test-demo";

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Input: " + input);
        String result = "Produce success!";
        // 遍历Map输出key，value
        for (Map.Entry<String, String> entry : input.entrySet()) {
            logger.log(entry.getKey() + " : " + entry.getValue());
        }
        try {
            SQSClientPool.sendMsg(QUEUE_URL, OBJECT_MAPPER.writeValueAsString(input));
        } catch (JsonProcessingException e) {
            result = "Produce failed!";
            logger.log(e.getMessage());
        }
        return result;
    }

}
