package com.goose.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@NoArgsConstructor
public class MessageDTO {
    String destination;
    String content;

    public MessageDTO(String destination, String content) throws IOException {
        this.destination = destination;
        setContent(content);
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setContent(String content) throws IOException {
        if (content.equals("goose")) {
            content = Files.readString(Path.of("src/main/resources/index.html"), StandardCharsets.UTF_8);
        } else {
            this.content = content;
        }
    }
}
