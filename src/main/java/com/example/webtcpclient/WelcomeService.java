package com.example.webtcpclient;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
@AllArgsConstructor
@Slf4j
public class WelcomeService {

    private final TcpClient tcpClient;

    public Mono<String> capitalize(String name) {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        tcpClient.doOnDisconnected(connection -> log.info("--- disconnected"))
                .connect()
                .flatMap(connection ->
                    connection.outbound().sendString(Mono.just(name))
                        .then(connection.inbound().receive().asString().next()
                                .doAfterTerminate(connection::dispose)
                                .flatMap(resp -> {
                                log.info("resp {}", resp);
                                completableFuture.complete(resp);
                            return Mono.empty();
                        }))
                        .then())
                .onErrorResume(ConnectException.class, e -> {
                    log.error(e.getMessage());
                    return Mono.error(e);
                })
                .onErrorResume(ReadTimeoutException.class, e -> {
                    log.error(e.getMessage());
                    return Mono.error(e);
                })
                .subscribe();

        return Mono.fromFuture(completableFuture);
    }

    public Mono<String> capitalize2(String name) throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        AtomicReference<String> holder = new AtomicReference<>();
        tcpClient.doOnDisconnected(connection -> log.info("--- disconnected"))
                .connect()
                .flatMap(connection ->
                    connection.outbound().sendString(Mono.just(name))
                            .then(connection.inbound().receive().asString().next().flatMap(bytes -> {
                                log.info("bytes {}", bytes);
                                holder.set(bytes);
                                System.out.println(1);
                                cdl.countDown();
                                System.out.println(2);
                                return Mono.empty();
                            }))
                            .then()
        ).subscribe();
        System.out.println(3);
        cdl.await(5, TimeUnit.SECONDS);
        System.out.println(4);
        System.out.println(holder.get());
        return Mono.just(holder.get());
    }

    public Mono<String> capitalize3(String name) throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        tcpClient
             .handle((in, out) -> {
                in.receive()
                .asString().next().then()
                .subscribe(s -> {
                    log.info("----3 {}", s);
                    System.out.println(1);
                    cdl.countDown();
                    System.out.println(2);
                });
            return out.sendByteArray(Mono.just(name.getBytes(StandardCharsets.UTF_8)))
                    .neverComplete();
        }).connect().then().subscribe();
        System.out.println(3);
        cdl.await(10, TimeUnit.SECONDS);
        System.out.println(4);
        return Mono.just("333");
    }

    public Mono<String> capitalize4(String name) throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("localhost" , 9000));
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream  = socket.getOutputStream();
            outputStream.write("hello".getBytes());
            outputStream.flush();
            InputStreamReader reader = new InputStreamReader(inputStream) ;
            char [] temChar  = new char[40];
            StringBuffer buffer = new StringBuffer( );

            while (reader.read(temChar) != -1){
                buffer.append(temChar);
                System.out.println(buffer.toString() +"\n");
            }
            System.out.println(1);
            cdl.countDown();
            System.out.println(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(3);
        cdl.await(5, TimeUnit.SECONDS);
        System.out.println(4);
        return Mono.just("123");
    }
}
