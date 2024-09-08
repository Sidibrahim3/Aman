package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.ResponseMessage;
import com.sidibrahim.Aman.dto.UserDto;
import com.sidibrahim.Aman.dto.request.UserToAgencyDto;
import com.sidibrahim.Aman.service.UserService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseMessage> getAllUsers() {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Users List Retrieved Successfully")
                .status(HttpStatus.OK.value())
                .data(userService.getAllUsers())
                .build());
    }

    @GetMapping("/test")
    public ResponseEntity<ResponseMessage> test(){
        return ResponseEntity.ok(ResponseMessage.builder()
                        .data("HEllo World Test Endpoint")
                        .message("Test Endpoint")
                        .status(HttpStatus.OK.value())
                .build());
    }

    @PostMapping
    @PreAuthorize("!hasAuthority('AGENT')")
    public ResponseEntity<ResponseMessage> save(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("User Saved Successfully")
                .status(HttpStatus.OK.value())
                .data(userService.addUser(userDto))
                .build());
    }

    @PostMapping("/add-to-agency")
    @PreAuthorize("!hasAuthority('AGENT')")
    public ResponseEntity<ResponseMessage> addUserToAgency(@RequestBody UserToAgencyDto userToAgencyDto) {

        return ResponseEntity.ok(ResponseMessage
                .builder()
                        .message(userService.addUserToAgency(userToAgencyDto))
                        .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/{agencyId}/agency")
    @PreAuthorize("!hasAuthority('AGENT')")
    public ResponseEntity<ResponseMessage> getAllUsersByAgencyId(@PathVariable Long agencyId) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Users List In Agency Retrieved Successfully")
                .status(HttpStatus.OK.value())
                .data(userService.getAllUsersByAgencyId(agencyId))
                .build());
    }
}
