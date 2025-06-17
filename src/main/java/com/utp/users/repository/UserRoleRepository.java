package com.utp.users.repository;

import com.utp.users.model.entity.UserRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface UserRoleRepository extends R2dbcRepository<UserRole, Long> {
  Flux<UserRole> findAllByIdUser(Long idUser);
}
