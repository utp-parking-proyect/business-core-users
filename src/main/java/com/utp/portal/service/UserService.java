package com.utp.portal.service;

import com.utp.portal.model.dto.UserLoginResponse;
import com.utp.portal.model.dto.UserPageResponse;
import com.utp.portal.model.dto.UserRegisterRequest;
import com.utp.portal.model.dto.UserResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
  Mono<UserPageResponse> findAll(int page, int size);
  Mono<UserResponse> findById(Long id);
  Mono<UserResponse> findAuthenticated();
  Mono<UserLoginResponse> findByUsername(String username);
  Mono<UserResponse> register(UserRegisterRequest request);
  Mono<UserResponse> assignRole(Long idUser, Long idRole);
  Mono<UserResponse> unassignRole(Long idUser, Long idRole);
  Flux<UserResponse> findByRole(String roleName, Long idCampus);
}
