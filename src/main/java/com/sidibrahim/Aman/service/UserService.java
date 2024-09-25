package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.UserDto;
import com.sidibrahim.Aman.dto.request.UserToAgencyDto;
import com.sidibrahim.Aman.entity.Agency;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.exception.GenericException;
import com.sidibrahim.Aman.mapper.AgencyMapper;
import com.sidibrahim.Aman.mapper.UserMapper;
import com.sidibrahim.Aman.repository.AgencyRepository;
import com.sidibrahim.Aman.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AgencyService agencyService;
    private final AgencyMapper agencyMapper;
    private final AgencyRepository agencyRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, AgencyService agencyService, AgencyMapper agencyMapper, AgencyRepository agencyRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.agencyService = agencyService;
        this.agencyMapper = agencyMapper;
        this.agencyRepository = agencyRepository;
    }

    @Transactional
    public UserDto addUser(UserDto userDto) {
        Optional<User> user = userRepository.findUserByPhoneNumber(userDto.getPhoneNumber());
        if (user.isPresent()) {
            throw new GenericException("User Already Exists");
        }
        String userPassword = userDto.getPassword();
        log.info("Adding new user");
        userDto.setPassword(passwordEncoder.encode(userPassword));
        log.info("user password: {}", userPassword);
        log.info("user password: {}", userDto.getPassword());
        User userEntity = userMapper.toUser(userDto);
        log.info("user entity: {}", userEntity);
        Agency agency = agencyRepository.findById(userDto.getAgencyId())
                .orElseThrow(()->new GenericException("Agency Not Found"));
        userEntity.setEnabled(true);
        userEntity.setAgency(agency);
        userEntity.setCreateDate(LocalDateTime.now());
        userEntity.setUpdateDate(LocalDateTime.now());
        User savedUser = userRepository.save(userEntity);
        log.info("saved user: {}", savedUser);
        return userMapper.toUserDto(savedUser);
    }

    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllUsers(pageable);
        return userPage.map(userMapper::toUserDto);
    }

    @Transactional
    public String addUserToAgency(UserToAgencyDto userToAgencyDto) {
        User user = userRepository.findById(userToAgencyDto.getUserId()).orElseThrow(()-> new GenericException("User Not Found"));
        Agency agency = agencyMapper.toAgency(agencyService.getById(userToAgencyDto.getAgencyId()));
        user.setAgency(agency);
        userRepository.save(user);
        return "User Added To agency Successfully";
    }

    public List<UserDto> getAllUsersByAgencyId(Long agencyId) {
        List<User> users = userRepository.findByAgencyId(agencyId);
        return users.stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    public UserDto getUSerById(Long id){
        return userMapper.toUserDto(Objects.requireNonNull(userRepository.findById(id).orElse(null)));
    }

    public UserDto getUserByPhoneNumber(String phoneNumber) {
        return userMapper.toUserDto(userRepository
                .findUserByPhoneNumber(phoneNumber)
                .orElseThrow(
                        () -> new GenericException("User Not Found with phoneNumber" + phoneNumber)));
    }
}
