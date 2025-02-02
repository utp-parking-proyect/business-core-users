package com.utp.users.service;

import com.utp.users.model.dto.UserDto;
import com.utp.users.model.dto.UserLoginDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Flux<UserDto> findAll();
    Mono<UserDto> findById(Long id);
    Mono<UserLoginDto> findByUsername(String username);
}
