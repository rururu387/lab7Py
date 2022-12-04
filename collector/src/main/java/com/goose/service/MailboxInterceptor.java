package com.goose.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMultipart;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MailboxInterceptor {
    private static final Logger SECCESS_LOGGER = LoggerFactory.getLogger("SUCCESS");
    private static final Logger ERROR_LOGGER = LoggerFactory.getLogger("ERROR");
    private Properties properties;
    private Session session;
    private int recvRate;
    private final ScheduledExecutorService scheduledExecutor = new ScheduledThreadPoolExecutor(1);

    public MailboxInterceptor() {
        try {
            var config = ConfigProvider.getConfig();
            recvRate = Integer.parseInt(config.getValue("app.mail.receive_frequency_seconds", String.class));
            properties = new Properties();
            for (var propertyName : config.getPropertyNames()) {
                properties.put(propertyName, config.getValue(propertyName, String.class));
            }
            session = Session.getInstance(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void onStart(@Observes StartupEvent ev) {
        scheduledExecutor.scheduleAtFixedRate(this::interceptMessages, 0, recvRate, SECONDS);
    }

    private String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart)  throws MessagingException, IOException{
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append("\n").append(Jsoup.parse(html).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart){
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    void handleMessage(Message message) throws MessagingException, IOException {
        var pattern = Pattern.compile("^\\[Ticket #(\\d*)\\] Mailer$");
        var matcher = pattern.matcher(message.getSubject());
        if (matcher.matches()) {
            var logStr = "{ \"subject id\": " + matcher.group() + ", \"message content\": " +
                    getTextFromMessage(message) + " }";
            SECCESS_LOGGER.info(logStr);
            return;
        }
        var logStr = "{ \"message content\": " +
                getTextFromMessage(message) + " }";
        ERROR_LOGGER.info(logStr);
    }

    void interceptMessages() {
        LocalDateTime curTime = LocalDateTime.now();
        try (var store = session.getStore()) {
            var host = properties.getProperty("mail.host");
            var port = Integer.parseInt(properties.getProperty("mail.port"));
            var user = properties.getProperty("mail.user");
            var password = properties.getProperty("mail.password");
            store.connect(host, port, user, password);
            var inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            var messageIterator = inbox.getMessageCount();

            if (messageIterator < 1) {
                return;
            }

            var curMessage = inbox.getMessage(messageIterator);
            while (messageIterator > 0 &&
                    curMessage.getReceivedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .isAfter(curTime.minusSeconds(recvRate))) {
                handleMessage(curMessage);
                messageIterator--;
                curMessage = inbox.getMessage(messageIterator);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
