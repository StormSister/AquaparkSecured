
package com.example.aquaparksecured.email;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${aws.ses.from-address}")
    private String fromAddress;

    @Value("${aws.ses.access-key}")
    private String accessKey;

    @Value("${aws.ses.secret-key}")
    private String secretKey;

    private final ResourceLoader resourceLoader;

    public EmailService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void sendEmailWithAttachments(String to, String subject, String body, List<String> attachmentPaths) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "email-smtp.eu-north-1.amazonaws.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(accessKey, secretKey);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAddress));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);
        multipart.addBodyPart(messageBodyPart);
        
        for (String path : attachmentPaths) {
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            File file = new File(path);
            if (!file.exists()) {
                throw new IOException("File not found: " + path);
            }
            DataSource source = new FileDataSource(file);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(file.getName());
            multipart.addBodyPart(attachmentBodyPart);
        }

        message.setContent(multipart);

        Transport.send(message);
    }
}
