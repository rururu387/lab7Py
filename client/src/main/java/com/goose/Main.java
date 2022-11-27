package com.goose;

import com.goose.dto.MessageDTO;
import com.goose.service.ClientSocketService;

import java.util.Scanner;

public class Main {
    private static ClientSocketService clientSocketService;

    public static void main(String[] args) throws Exception {
        var sc = new Scanner(System.in);
        while (true) {
            System.out.println("Enter destination or :q to quit: ");
            var destination = sc.nextLine();
            if (destination.equals(":q")) {
                break;
            }
            System.out.println("Enter message. Type <Enter>:wq<Enter> to finish: ");
            var contentBuilder = new StringBuilder();
            var messageStr = sc.nextLine();
            while (!messageStr.equals(":wq")) {
                contentBuilder.append(messageStr);
                messageStr = sc.nextLine();
            }
            clientSocketService = new ClientSocketService();
            var message = new MessageDTO(destination, contentBuilder.toString());
            Thread.sleep(200);
            clientSocketService.sendMessage(message);
            clientSocketService.clear();
        }
    }
}
