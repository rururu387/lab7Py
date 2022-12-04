package com.goose.service;

import com.goose.dto.MessageDTO;
import io.quarkus.runtime.StartupEvent;
import org.apache.commons.lang3.ArrayUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@ApplicationScoped
public class TCPSocketService {
    MailService mailService;

    public TCPSocketService(MailService mailService) throws Exception {
        this.mailService = mailService;
    }

    Jsonb jsonMapper = JsonbBuilder.create();

    void onStart(@Observes StartupEvent ev) throws IOException {
        try (ServerSocket socket = new ServerSocket(1027)) {
            while (true) {
                var acceptSock = socket.accept();
                var in = acceptSock.getInputStream();

                var messageByteList = new ArrayList<Byte>();
                int status;
                while ((status = in.read()) != -1) {
                    messageByteList.add((byte)status);
                }
                var message = new String(ArrayUtils.toPrimitive(messageByteList.toArray(new Byte[messageByteList.size()])),
                        StandardCharsets.UTF_8);
                var messageDTO = jsonMapper.fromJson(message, MessageDTO.class);
                mailService.sendMessage(messageDTO.getDestination(), messageDTO.getContent());
            }
        }
    }
}
