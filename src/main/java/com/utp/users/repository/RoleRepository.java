package com.utp.users.repository;

import com.utp.users.model.entity.Role;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface RoleRepository extends R2dbcRepository<Role, Long> {
}
