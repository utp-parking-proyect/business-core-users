package com.utp.portal.repository;

import com.utp.portal.model.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
  Mono<User> findByUsername(String username);
  Flux<User> findAllBy(Pageable pageable);
  Mono<Boolean> existsByUsername(String username);
  Mono<Boolean> existsByDni(String dni);
  Mono<Boolean> existsByInstitutionalEmail(String institutionalEmail);

  @Query("""
      SELECT u.* FROM users u
      JOIN user_roles ur ON ur.id_user = u.id_user
      JOIN role r ON r.id_role = ur.id_role
      WHERE r.name_role = :roleName
        AND (:idCampus IS NULL OR u.id_campus = :idCampus)
      """)
  Flux<User> findAllByRoleNameAndOptionalCampus(@Param("roleName") String roleName,
                                                 @Param("idCampus") Long idCampus);
}
