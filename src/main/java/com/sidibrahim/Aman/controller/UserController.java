package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.PaginationData;
import com.sidibrahim.Aman.dto.ResponseMessage;
import com.sidibrahim.Aman.dto.UserDto;
import com.sidibrahim.Aman.dto.request.UserToAgencyDto;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.mapper.UserMapper;
import com.sidibrahim.Aman.repository.UserRepository;
import com.sidibrahim.Aman.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserMapper userMapper, UserRepository userRepository) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    @GetMapping()
  /*  @PreAuthorize("hasAuthority('SUPER_ADMIN')")*/
    @PreAuthorize("hasAuthority({'SUPER_ADMIN','})")
    public ResponseEntity<ResponseMessage> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "5") int size) {
        Page<UserDto> users = userService.getAllUsers(page, size);
        PaginationData paginationData = new PaginationData(users);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Users List Retrieved Successfully")
                .status(HttpStatus.OK.value())
                .data(users.getContent())
                .meta(paginationData)
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

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseMessage> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(ResponseMessage.builder()
                .data(userService.getUSerById(userId))
                .message("Fetched User Successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
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
    @GetMapping("/by-phoneNumber")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseMessage> getUserByPhoneNumber(@RequestParam(name = "phoneNumber") String phoneNumber) {
        return ResponseEntity.ok(ResponseMessage.builder()
                .message("User Retrieved Successfully")
                .data(userService.getUserByPhoneNumber(phoneNumber))
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto user = userService.getUserByPhoneNumber(authentication.getName());
        log.info("User PhoneNumber :=> "+authentication.getName());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/change-email")
    public ResponseEntity<UserDto> changeEmail(@RequestParam(name = "email",required = false) String email){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (email!=null){
            User user = userRepository.findUserByPhoneNumber(authentication.getName()).get();
            user.setEmail(email);
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(userMapper.toUserDto(savedUser));
        }
        return ResponseEntity.badRequest().build();
    }

}
