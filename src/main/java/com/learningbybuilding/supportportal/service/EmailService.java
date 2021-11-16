package com.learningbybuilding.supportportal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Value("${spring.mail.username}")
    String fromEmailId;

    private final JavaMailSender mailSender;

    public void sendEmail(String firstName, String password, String email) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        // no need of it email will go from the account specified in application.yml
        //mailMessage.setFrom(fromEmailId);
        mailMessage.setTo(email);
        mailMessage.setSubject("New Registration For Support Portal");
        mailMessage.setText("Hello " + firstName + ",\n\n" + "Your password is:- " + password + "\n\n" + "Regards,\n Support Portal Team");

        //mailSender.send(mailMessage);
    }

}
