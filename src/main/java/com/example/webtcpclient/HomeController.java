package com.example.webtcpclient;

import io.netty.handler.timeout.ReadTimeoutException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.ArrayList;

@RestController
@AllArgsConstructor
public class HomeController {

    private final WelcomeService welcomeService;

    private final TcpGateway gateway;

    private final LogHandlerCustomizer logHandlerCustomizer;

    @GetMapping("/")
    public Mono<String> home(@RequestParam(defaultValue = "Unknown") String name) {
        return Mono.just(name.toUpperCase());
    }

    @GetMapping("/tcp")
    public Mono<String> home1(@RequestParam(defaultValue = "Unknown") String name) {
        return welcomeService.capitalize(name);
    }

    @GetMapping("/tcp2")
    public Mono<String> home5(@RequestParam(defaultValue = "Unknown") String name) {
        return welcomeService.capitalize5(name);
    }

    @GetMapping("/tcp3")
    public Mono<String> home6(@RequestParam(defaultValue = "Unknown") String name) {
        return welcomeService.capitalize6(name)
                .onErrorReturn(ReadTimeoutException.class, "ReadTimeoutException")
                .onErrorReturn(ConnectException.class, "ConnectException");
    }

    // add Success/Error listener + circuit breaker resilient4j
    @GetMapping("/v2/tcp")
    public Mono<String> home2(@RequestParam(defaultValue = "Unknown") String name) {
        TcpRequest<String, String> req = new TcpRequest<String, String>(name,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()).addHandler(logHandlerCustomizer);
        return gateway.sendTcpServer(req);
    }

}
