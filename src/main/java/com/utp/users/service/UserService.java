package com.utp.users.service;

import com.utp.users.model.dto.UserLoginResponse;
import com.utp.users.model.dto.UserPageResponse;
import com.utp.users.model.dto.UserResponse;
import reactor.core.publisher.Mono;

public interface UserService {
  Mono<UserPageResponse> findAll(int page, int size);

  Mono<UserResponse> findById(Long id);

  Mono<UserLoginResponse> findByUsername(String username);
}
