package org.vivalink.pools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQSClientPool {

    public static final String ACCESS_KEY = "XXXX";
    public static final String SECRET_KEY = "XXXX";
    public static final Integer MAX_CONNECTION = 100;
    private static final SdkHttpClient httpClient;

    private static SqsClient sqsClient;
    private static final StaticCredentialsProvider credentialsProvider;

    public static final Logger LOG = LoggerFactory.getLogger(SQSClientPool.class);

    static {
        // AWS Credentials
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        credentialsProvider = StaticCredentialsProvider.create(awsCreds);

        //  Apache HTTP Client
        httpClient = ApacheHttpClient.builder()
                .maxConnections(MAX_CONNECTION)
                .connectionTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 获取SQSClient实例
     * @return SQSClient实例
     */
    public  static SqsClient getSqsClient(){
        // 双检锁 类似于JAVA V1版本
        if (sqsClient == null){
            synchronized (SQSClientPool.class){
                if (sqsClient == null){
                    sqsClient = SqsClient.builder()
                            .httpClient(httpClient)
                            .credentialsProvider(credentialsProvider)
                            .build();
                }
            }
        }
        return sqsClient;
//        return SqsClient.builder()
//                .httpClient(httpClient)
//                .credentialsProvider(credentialsProvider)
//                .build();
    }



    public static SendMessageResponse sendMsg(String queueUrl, String messageBody){
        return sendMsg(queueUrl, messageBody, null, 0);
    }

    public static SendMessageResponse sendMsg(String queueUrl, String messageBody, int delaySecond){
        return sendMsg(queueUrl, messageBody, null, delaySecond);
    }


    /**
     * 发送消息到SQS队列中
     * @param queueUrl 队列地址
     * @param messageBody 消息主体
     * @param messageAttributes 消息属性
     * @param delaySecond 延迟发送秒数
     * @return 消息返回
     */
    public static SendMessageResponse sendMsg(String queueUrl, String messageBody,
                                              Map<String, MessageAttributeValue> messageAttributes, int delaySecond){
        if (messageAttributes == null){
            messageAttributes = new HashMap<>();
        }
        messageAttributes.put("SendTime", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(String.valueOf(System.currentTimeMillis()))
                .build());
        SendMessageRequest messageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .messageAttributes(messageAttributes)
                .delaySeconds(delaySecond)
                .build();
        return  getSqsClient().sendMessage(messageRequest);
    }


    /**
     * 消费SQS队列中消息
     * @param queueUrl 队列地址
     * @return  ReceiveMessageResponse
     */
    public static List<Message> receiveMsg(String queueUrl){
        ReceiveMessageRequest messageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(10) // 长轮询时长。
                .visibilityTimeout(30)  // 不可见时长，也可以在队列中配置。
                .messageAttributeNames("All") // 返回所有属性。
                .build();
        getSqsClient();
        List<Message> messages = sqsClient.receiveMessage(messageRequest).messages();
        // 清除消息
        for (Message message : messages){
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }
        return messages;
    }


}
