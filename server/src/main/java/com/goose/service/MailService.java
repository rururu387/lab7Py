package com.goose.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MailService {
    private Mailer mailer;

    public MailService(Mailer mailer) {
        this.mailer = mailer;
    }

    public void sendMessage(String destination, String content) {
        try {
            mailer.send(Mail.withText(destination, "Geese are cool!", content));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
