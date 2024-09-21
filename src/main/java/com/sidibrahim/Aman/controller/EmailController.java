package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.EmailDetailsDto;
import com.sidibrahim.Aman.service.SendMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class EmailController {

    private final SendMailService emailService;

    public EmailController(SendMailService emailService) {
        this.emailService = emailService;
    }


    @PostMapping("/sendMail")
    public void sendMail(@RequestBody EmailDetailsDto details) {

        log.info("send a simple mail");
        emailService.sendSimpleMail(details);
    }

}