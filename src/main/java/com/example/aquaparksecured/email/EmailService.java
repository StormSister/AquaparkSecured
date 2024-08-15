
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.services.ses.SesClient;
//import software.amazon.awssdk.services.ses.model.*;
//
//@Service
//public class EmailService {
//
//    private final SesClient sesClient;
//
//    @Value("${aws.ses.from-address}")
//    private String fromAddress;
//
//    @Autowired
//    public EmailService(SesClient sesClient) {
//        this.sesClient = sesClient;
//    }
//
//    public void sendEmail(String from, String to, String subject, String body) {
//        Destination destination = Destination.builder()
//                .toAddresses(to)
//                .build();
//
//        Content content = Content.builder()
//                .data(body)
//                .build();
//
//        Content sub = Content.builder()
//                .data(subject)
//                .build();
//
//        Message message = Message.builder()
//                .body(Body.builder().text(content).build())
//                .subject(sub)
//                .build();
//
//        SendEmailRequest emailRequest = SendEmailRequest.builder()
//                .source(from)
//                .destination(destination)
//                .message(message)
//                .build();
//
//        sesClient.sendEmail(emailRequest);
//    }
//}
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${aws.ses.from-address}")
    private String fromAddress;

    private final ResourceLoader resourceLoader;

    public EmailService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    private static final String ATTACHMENT_PATH = "classpath:tickets/ticket_3.pdf";

    public void sendEmailWithAttachments(String to, String subject, String body) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "email-smtp.eu-north-1.amazonaws.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("AKIAX5ZI57PQAAK3QQEN", "BNi8T3iWGAS8w+8+8ZgK2tRZ8W3FS2/qKWM04tEcwKsu");
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


        Resource resource = resourceLoader.getResource(ATTACHMENT_PATH);
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + ATTACHMENT_PATH);
        }

        File file = resource.getFile();
        messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(file);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(file.getName());
        multipart.addBodyPart(messageBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }
}
