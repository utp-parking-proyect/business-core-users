package com.utp.users.repository;

import com.utp.users.model.entity.Campus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface CampusRepository extends R2dbcRepository<Campus, Long> {
}
