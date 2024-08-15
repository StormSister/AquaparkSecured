//package com.example.aquaparksecured.email;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import jakarta.mail.MessagingException;
//import java.util.List;
//
//@RestController
//public class EmailController {
//
//    private final EmailService emailService;
//
//    @Autowired
//    public EmailController(EmailService emailService) {
//        this.emailService = emailService;
//    }
//
//    @GetMapping("/send-email")
//    public String sendEmail(@RequestParam String to,
//                            @RequestParam String subject,
//                            @RequestParam String body,
//                            @RequestParam(required = false) List<String> attachments) {
//        try {
//            System.out.println(to + ' ' +  subject+ " " + body);
//            emailService.sendEmailWithAttachments(to, subject, body, attachments);
//            return "Email sent!";
//        } catch (MessagingException e) {
//            e.printStackTrace();
//            return "Failed to send email.";
//        }
//    }
//}
package com.example.aquaparksecured.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.MessagingException;
import java.io.IOException;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-email")
    public String sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmailWithAttachments(
                    emailRequest.getTo(),
                    emailRequest.getSubject(),
                    emailRequest.getBody()
            );
            return "Email sent!";
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "Failed to send email.";
        }
    }
}


