package org.vivalink;

import com.amazonaws.AbortedException;
import io.netty.handler.codec.http2.Http2MultiplexActiveStreamsException;
import org.junit.jupiter.api.Test;
import org.vivalink.pools.SQSClientPool;

import java.io.IOException;


public class SendMsgTest {

    public static  final String QUEUE_URL = "https://sqs.ap-northeast-1.amazonaws.com/471112901071/test-demo";

    @Test
    public void sendMsg() {
        SQSClientPool.sendMsg(QUEUE_URL, "==>Hello World! 001");
    }


    @Test
    public void sendMsg2() {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    SQSClientPool.sendMsg(QUEUE_URL, "===>hello world！messageID： " + j);
                }
            }).start();
        }
    }
}
