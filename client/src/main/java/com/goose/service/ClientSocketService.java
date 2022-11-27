package com.goose.service;

import com.goose.dto.MessageDTO;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientSocketService {
    Jsonb jsonMapper = JsonbBuilder.create();
    Socket socket;

    public ClientSocketService() throws IOException {
        socket = SocketFactory.getDefault().createSocket("localhost", 1027);
    }

    public void sendMessage(MessageDTO message) throws IOException {
        var outputStream = socket.getOutputStream();
        outputStream.write(jsonMapper.toJson(message).getBytes(StandardCharsets.UTF_8));
        outputStream.close();
    }

    public void clear() throws IOException {
        socket.close();
    }
}
