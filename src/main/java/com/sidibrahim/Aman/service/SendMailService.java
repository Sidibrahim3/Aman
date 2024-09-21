package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.EmailDetailsDto;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@Slf4j
public class SendMailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public SendMailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendSimpleMail(EmailDetailsDto details) {
        try {
            log.info("send a simple mail without attachment file");
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getEmailBody());
            mailMessage.setSubject(details.getEmailSubject());

            javaMailSender.send(mailMessage);
        } catch (Exception exception) {
            log.error("error while sending the mail"+ exception.getMessage());
        }
    }
    public void sendEmailWithAttachment(EmailDetailsDto details) {
        try {
            log.info("Sending email with attachment to {}", details.getRecipient());

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // Set to true to indicate that the message will have attachments
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(sender);
            helper.setTo(details.getRecipient());
            helper.setSubject(details.getEmailSubject());
            helper.setText(details.getEmailBody());

            // Attach the file if attachment data is provided
            if (details.getAttachment() != null && details.getAttachmentFilename() != null) {
                ByteArrayResource attachment = new ByteArrayResource(details.getAttachment());
                helper.addAttachment(details.getAttachmentFilename(), attachment);
                log.info("Attached file {} to the email", details.getAttachmentFilename());
            }

            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to {}", details.getRecipient());

        } catch (Exception exception) {
            log.error("Error while sending email with attachment: {}", exception.getMessage(), exception);
            // Optionally, rethrow the exception or handle it as per your application's requirements
            throw new RuntimeException("Failed to send email with attachment", exception);
        }
    }
}
