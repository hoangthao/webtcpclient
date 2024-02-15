package com.example.webtcpclient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;

import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;

@Component
@AllArgsConstructor
@Getter
@Slf4j
public class TcpGateway {

    private final TcpClient tcpClientPrimary;
    private final TcpClient tcpClientSecondary;

    @CircuitBreaker(name = "sendTcpServer", fallbackMethod = "sendTcpServerSecondary")
    public Mono<String> sendTcpServer(TcpRequest<String, String> originReq) {
        return this.send(tcpClientPrimary, originReq);
    }

    private Mono<String> sendTcpServerSecondary(TcpRequest<String, String> originReq, final Exception ex) {
        log.info("Primary connection failed with err {} - failing over to secondary", ex.getMessage());
        if (ex instanceof io.netty.handler.timeout.ReadTimeoutException) {
            return Mono.error(ex);
        }
        return this.send(tcpClientSecondary, originReq);
    }

    private Mono<String> send(final TcpClient tcpClient, final TcpRequest<String, String> originReq) {
        final TcpRequest<String, String> request = this.customize(originReq);
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        tcpClient.doOnDisconnected(connection -> log.info("--- disconnected"))
                .connect()
                .flatMap(connection ->
                        connection.outbound().sendString(Mono.just(request.getBody()))
                                .then(connection.inbound().receive().asString().next()
                                        .doAfterTerminate(connection::dispose)
                                        .flatMap(resp -> {
                                            log.info("resp {}", resp);
                                            completableFuture.complete(resp);
                                            request.getSuccessListenerList().forEach(e -> {
                                                e.onSuccess(request.getBody(), resp);
                                            });
                                            return Mono.empty();
                                        }))
                                .then())
                .onErrorResume(ConnectException.class, e -> {
                    log.error("Connect Issue:: {}", e.getMessage());
                    request.getErrorListeners().forEach(i -> {
                        i.onError(request.getBody(), e);
                    });
                    return Mono.error(e);
                })
                .onErrorResume(ReadTimeoutException.class, e -> {
                    log.error("Read Timeout Issue:: {}", e.getMessage());
                    request.getErrorListeners().forEach(i -> {
                        i.onError(request.getBody(), e);
                    });
                    return Mono.error(e);
                })
                .subscribe();

        return Mono.fromFuture(completableFuture);
    }

    private TcpRequest<String, String> customize(final TcpRequest<String, String> originReq) {
        TcpRequest<String, String> request = originReq;
        for (final HandlerCustomizer customizer : originReq.getHandlerCustomizers()) {
            request = customizer.customize(request);
        }
        return request;
    }
}
