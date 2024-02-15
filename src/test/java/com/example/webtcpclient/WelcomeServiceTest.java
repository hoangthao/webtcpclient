package com.example.webtcpclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
//import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
class WelcomeServiceTest {

    @InjectMocks
    WelcomeService service;

    @Mock
    TcpClient tcpClient;

    @Mock
    Connection connection;

    @Mock
    private NettyOutbound outbound;

    @Mock
    private NettyInbound inbound;

    @Test
    void capitalize5() {

        when(tcpClient.doOnDisconnected(any())).thenReturn(tcpClient);
        when(tcpClient.connect()).thenAnswer(inv -> Mono.just(connection));
        when(connection.outbound()).thenReturn(outbound);
        when(connection.inbound()).thenReturn(inbound);
        when(outbound.sendString(any())).thenReturn(outbound);
        when(outbound.then()).thenAnswer(inv -> Mono.empty());

        final Flux<String> byteBuffFlux = ByteBufFlux.from(Mono.just("THAO"));
        when(inbound.receive()).thenReturn(ByteBufFlux.fromString(byteBuffFlux));

        Mono<String> response = service.capitalize5("thao");
        StepVerifier.create(response)
                .assertNext(result -> {
                    assertEquals("THAO", result);
                }).verifyComplete();
    }
}