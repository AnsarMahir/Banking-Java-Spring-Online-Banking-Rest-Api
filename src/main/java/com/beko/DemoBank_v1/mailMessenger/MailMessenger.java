package com.beko.DemoBank_v1.mailMessenger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailMessenger {

    private final JavaMailSender mailSender;

    @Autowired
    public MailMessenger(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void htmlEmailMessenger(String from, String toMail, String subject, String body) throws MessagingException {
        // Set Mime Message:
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper htmlMessage = new MimeMessageHelper(message, true);

        // Set Mail Attributes / Properties:
        htmlMessage.setTo(toMail);
        htmlMessage.setFrom(from);
        htmlMessage.setSubject(subject);
        htmlMessage.setText(body, true);
        // Send Message:
        mailSender.send(message);
    }
    // End Of HTML Email Message Method
}
