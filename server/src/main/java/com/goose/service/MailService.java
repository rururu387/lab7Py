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

    /*public void sendMessage(String destination, String content) throws MessagingException {
        Properties props = System.getProperties();
        props.put("mail.smtps.host", "smtp.mailgun.org");
        props.put("mail.smtps.auth", "true");

        Session session = Session.getInstance(props, null);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("ol.lavr088.lav@mail.ru"));

        InternetAddress[] addrs = InternetAddress.parse("ol.lavr088.lav@mail.ru", false);
        msg.setRecipients(Message.RecipientType.TO, addrs);

        msg.setSubject("Geese are cool");
        msg.setText(content);
        msg.setSentDate(new Date());

        SMTPTransport t =
                (SMTPTransport) session.getTransport("smtps");
        t.connect("smtp.mailgun.org", "postmaster@sandbox5a86f0e0752243cda041f3a6102b25c8.mailgun.org", "3941d6e574dc1322ece8f0017ef2cedd-69210cfc-0811c938");
        t.sendMessage(msg, msg.getAllRecipients());

        System.out.println("Response: " + t.getLastServerResponse());

        t.close();
    }*/

    public void sendMessage(String destination, String content) {
        try {
            mailer.send(Mail.withText(destination, "Geese are cool!", content));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
