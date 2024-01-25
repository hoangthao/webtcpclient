package com.example.webtcpclient;

public interface TcpErrorListener {

    void onError(String request, Exception ex);
}
