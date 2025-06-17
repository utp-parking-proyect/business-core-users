package com.utp.users.controller;

import com.utp.users.model.dto.UserDto;
import com.utp.users.model.dto.UserLoginDto;
import com.utp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class UserController {

  private final UserService userService;

  @GetMapping
  public Flux<UserDto> getAllUsers() {
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public Mono<UserDto> getUserById(@PathVariable Long id) {
    return userService.findById(id);
  }

  @GetMapping("/username/{username}")
  public Mono<UserLoginDto> getUserByUsername(@PathVariable String username) {
    return userService.findByUsername(username);
  }
}
