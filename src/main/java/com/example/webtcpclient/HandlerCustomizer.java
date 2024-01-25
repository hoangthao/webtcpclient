package com.example.webtcpclient;

public interface HandlerCustomizer {

    <U,V> TcpRequest<U,V> customize(TcpRequest<U,V> tcpRequest);
}
