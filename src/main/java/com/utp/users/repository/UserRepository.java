package com.utp.users.repository;

import com.utp.users.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {
  Mono<User> findByUsername(String username);
  Flux<User> findAllBy(Pageable pageable);
}
