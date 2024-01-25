package com.example.webtcpclient;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class TcpRequest <U, V>{

    private final U body;

    private final List<TcpSuccessListener> successListenerList;
    private final List<TcpErrorListener> errorListeners;
    private final List<HandlerCustomizer> handlerCustomizers;

    public TcpRequest<U, V> addSuccessListener(TcpSuccessListener listener) {
        successListenerList.add(listener);
        return this;
    }

    public TcpRequest<U, V> addErrorListener(TcpErrorListener listener) {
        errorListeners.add(listener);
        return this;
    }

    public TcpRequest<U, V> addHandler(HandlerCustomizer handlerCustomizer) {
        handlerCustomizers.add(handlerCustomizer);
        return this;
    }
}
