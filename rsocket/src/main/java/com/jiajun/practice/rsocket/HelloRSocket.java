package com.jiajun.practice.rsocket;

import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

/**
 * @author jiajun
 */
public class HelloRSocket {

    @Test
    public void requestResponseTest() throws Exception {
        new Thread(() -> {
            // 创建一个RSocket Server
            RSocketServer.create(
                    SocketAcceptor.forRequestResponse(payload -> {
                                System.out.println("receive: " + payload.getDataUtf8());
                                return Mono.just(DefaultPayload.create("pong. ", System.currentTimeMillis() + ""));
                            }
                    ))
                    .payloadDecoder(PayloadDecoder.DEFAULT)
                    .bind(TcpServerTransport.create(7878))
                    .block()
                    .onClose()
                    .block();
        }, "SERVER").start();

        TimeUnit.SECONDS.sleep(2);

        RSocket socketClient = RSocketConnector.create()
                .payloadDecoder(PayloadDecoder.DEFAULT)
                .connect(TcpClientTransport.create(7878))
                .block();
        socketClient.requestStream(DefaultPayload.create("ping"));
    }
}
