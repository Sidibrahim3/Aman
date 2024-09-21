package com.sidibrahim.Aman.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetailsDto {

    private String recipient;
    private String emailBody;
    private String emailSubject;
    private byte[] attachment;          // The file data as byte array
    private String attachmentFilename;
}