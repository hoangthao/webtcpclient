package com.example.webtcpclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class ConnectionConfig {

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("tcpConnectionProvider")
                .maxConnections(5)
                .pendingAcquireTimeout(Duration.ofSeconds(5))
                .metrics(true)
                .build();
    }

    @Bean @Primary
    @Qualifier("tcpClientPrimary")
    public TcpClient tcpClientPrimary(ConnectionProvider connectionProvider) {
//        int maxFrameLength = Integer.MAX_VALUE;
//        int lengthFieldOffset = 0;
//        int lengthFieldLength = 2;
//        int lengthAdjustment = 0;
//        int initialBytesToStrip = 2;
        return TcpClient.create(connectionProvider)
                .host("localhost").port(9000)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .doOnConnected( connection ->
                        connection
                                .addHandlerFirst(new ReadTimeoutHandler(8, TimeUnit.SECONDS))
                                .addHandlerFirst(new LoggingHandler(LogLevel.INFO))
//                                .addHandlerLast(new LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset,
//                                        lengthFieldLength, lengthAdjustment, initialBytesToStrip))
                ).wiretap(true);
    }

    @Bean
    @Qualifier("tcpClientSecondary")
    public TcpClient tcpClientSecondary(ConnectionProvider connectionProvider) {
        return TcpClient.create(connectionProvider)
                .host("localhost").port(9001)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .doOnConnected( connection ->
                                connection
                                        .addHandlerFirst(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                                        .addHandlerFirst(new LoggingHandler(LogLevel.INFO))
                ).wiretap(true);
    }
}
