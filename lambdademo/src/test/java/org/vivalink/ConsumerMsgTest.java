package org.vivalink;

import org.junit.jupiter.api.Test;
import org.vivalink.pools.SQSAsyncClientPool;

public class ConsumerMsgTest {

    public static  final String QUEUE_URL = "https://sqs.ap-northeast-1.amazonaws.com/471112901071/test-demo";
    @Test
    public void testConsumerMsg() {
        // 5个线程同时消费
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                SQSAsyncClientPool.receiveMsg(QUEUE_URL);
            }).start();
        }
        while (true) {

        }
    }
}
