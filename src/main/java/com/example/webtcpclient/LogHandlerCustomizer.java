package com.example.webtcpclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogHandlerCustomizer implements TcpErrorListener, TcpSuccessListener, HandlerCustomizer{

    @Override
    public void onError(String request, Exception ex) {
        log.error("=====req {} with err {}", request, ex.getMessage());
    }

    @Override
    public void onSuccess(String request, String response) {
        log.info("=====req {} with resp {}", request, response);
    }


    @Override
    public <U, V> TcpRequest<U, V> customize(TcpRequest<U, V> tcpRequest) {
        return tcpRequest
                .addSuccessListener(this)
                .addErrorListener(this);
    }
}
