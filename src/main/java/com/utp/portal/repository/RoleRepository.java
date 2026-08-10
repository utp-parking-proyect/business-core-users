package com.utp.portal.repository;

import com.utp.portal.model.entity.Role;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface RoleRepository extends R2dbcRepository<Role, Long> {
}
