package com.utp.portal.repository;

import com.utp.portal.model.entity.UserRole;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleRepository extends R2dbcRepository<UserRole, Long> {
  Flux<UserRole> findAllByIdUser(Long idUser);
  Mono<Boolean> existsByIdUserAndIdRole(Long idUser, Long idRole);
  Mono<Void> deleteByIdUserAndIdRole(Long idUser, Long idRole);
}
