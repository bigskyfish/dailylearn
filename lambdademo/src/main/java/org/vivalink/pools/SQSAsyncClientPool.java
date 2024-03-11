package org.vivalink.pools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQSAsyncClientPool {

    public static final String ACCESS_KEY = "AKIAW3MEEMHH66CO2XH3";
    public static final String SECRET_KEY = "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";
    public static  final String QUEUE_URL = "https://sqs.ap-northeast-1.amazonaws.com/471112901071/test-demo";
    public static final Integer MAX_CONNECTION = 100;
    private static final SdkAsyncHttpClient httpClient;

    private static SqsAsyncClient sqsAsyncClient;
    private static final StaticCredentialsProvider credentialsProvider;

    public static final Logger LOG = LoggerFactory.getLogger(SQSAsyncClientPool.class);

    static {
        // AWS Credentials
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY);
        credentialsProvider = StaticCredentialsProvider.create(awsCreds);

        //  Apache HTTP Client
        httpClient = NettyNioAsyncHttpClient.builder()
                .maxConcurrency(MAX_CONNECTION)
                .connectionTimeout(Duration.ofSeconds(20))
                .build();
    }

    public static void getSqsAsyncClient(){
        if (sqsAsyncClient == null){
            synchronized (SQSAsyncClientPool.class){
                if (sqsAsyncClient == null){
                    sqsAsyncClient = SqsAsyncClient.builder()
                            .credentialsProvider(credentialsProvider)
                            .httpClient(httpClient)
                            .build();
                }
            }
        }
    }

    /**
     * 资源关闭
     */
    public static void close(){
        sqsAsyncClient.close();
        httpClient.close();
    }


    /**
     * 发送消息
     * @param queueUrl 队列地址
     * @param messageBody 消息内容
     * @param messageAttributes 消息属性
     * @param delaySecond  延迟发送秒数
     * @return 消息返回
     */
    public static void sendMsg(String queueUrl, String messageBody,
                               Map<String, MessageAttributeValue> messageAttributes, int delaySecond){
        if (messageAttributes == null){
            messageAttributes = new HashMap<>();
        }
        messageAttributes.put("SendTime", MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(String.valueOf(System.currentTimeMillis()))
                .build());
        getSqsAsyncClient();
        Map<String, MessageAttributeValue> finalMessageAttributes = messageAttributes;
        sqsAsyncClient.sendMessage(builder -> builder.queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .messageAttributes(finalMessageAttributes)
                        .delaySeconds(delaySecond))
                .whenComplete((resp, err) -> {
                    if (err != null){
                        LOG.error("Send Message Error: {}", err.getMessage());
                    }
                    LOG.info("Send Message Success: {}", resp);
                });
    }


    /**
     * 消费消息
     * @param queueUrl 队列地址
     * @return 消息返回
     */
    public static List<Message> receiveMsg(String queueUrl){
        getSqsAsyncClient();
        return sqsAsyncClient.receiveMessage(builder -> builder.queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(10) // 长轮询时长。
                        .visibilityTimeout(30)  // 不可见时长，也可以在队列中配置。
                        .messageAttributeNames("All") // 返回所有属性。
                        .build())
                .whenComplete((resp, err) -> {
                    if (err != null){
                        LOG.error("Receive Message Error: {}", err.getMessage());
                    }
                    LOG.info("Receive Message Success: {}", resp);
                    resp.messages().forEach(message -> {
                        // 执行业务逻辑
                        // 业务逻辑执行成功，清理Message
                        sqsAsyncClient.deleteMessage(DeleteMessageRequest.builder()
                                        .queueUrl(queueUrl)
                                        .receiptHandle(message.receiptHandle())
                                .build());
                    });
                })
                .join().messages();
    }

    public static void main(String[] args) {
        // 5个线程分别生产10条消息
//        for (int j = 0; j < 100; j++) {
//            sendMsg(QUEUE_URL, "Hello World!" + j, null, 0);
//        }
//        // 此处阻塞30s
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e){
//            LOG.error(e.getMessage());
//        }
        // 5个线程分别消费消息
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                while (true) {
                    receiveMsg(QUEUE_URL);
                }
            }).start();
        }
    }

}
