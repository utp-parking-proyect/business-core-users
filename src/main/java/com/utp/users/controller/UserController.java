package com.utp.users.controller;

import com.utp.users.api.UsersApi;
import com.utp.users.model.dto.UserLoginResponse;
import com.utp.users.model.dto.UserPageResponse;
import com.utp.users.model.dto.UserRegisterRequest;
import com.utp.users.model.dto.UserResponse;
import com.utp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class UserController implements UsersApi {

  private final UserService userService;

  @Override
  public Mono<ResponseEntity<UserPageResponse>> getAllUsers(Integer page, Integer size,
                                                            ServerWebExchange exchange) {
    return userService.findAll(page, size).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<UserResponse>> getUserById(Long id, ServerWebExchange exchange) {
    return userService.findById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @Override
  public Mono<ResponseEntity<UserLoginResponse>> getUserByUsername(String username,
                                                                   ServerWebExchange exchange) {
    return userService.findByUsername(username)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @Override
  public Mono<ResponseEntity<UserResponse>> registerUser(Mono<UserRegisterRequest> userRegisterRequest,
                                                          ServerWebExchange exchange) {
    return userRegisterRequest
        .flatMap(userService::register)
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
  }

  @Override
  public Mono<ResponseEntity<UserResponse>> assignRole(Long id, Long idRole,
                                                       ServerWebExchange exchange) {
    return userService.assignRole(id, idRole).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<UserResponse>> unassignRole(Long id, Long idRole,
                                                         ServerWebExchange exchange) {
    return userService.unassignRole(id, idRole).map(ResponseEntity::ok);
  }
}
