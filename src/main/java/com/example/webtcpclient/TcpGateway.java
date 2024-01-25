package com.example.webtcpclient;

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

    private final TcpClient tcpClient;

    public Mono<String> send(TcpRequest<String, String> originReq) {
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
                    log.error(e.getMessage());
                    request.getErrorListeners().forEach(i -> {
                        i.onError(request.getBody(), e);
                    });
                    return Mono.error(e);
                })
                .onErrorResume(ReadTimeoutException.class, e -> {
                    log.error(e.getMessage());
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
