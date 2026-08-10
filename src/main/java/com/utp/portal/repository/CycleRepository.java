package com.utp.portal.repository;

import com.utp.portal.model.entity.Cycle;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface CycleRepository extends R2dbcRepository<Cycle, Long> {

  @Query(value = """
      SELECT * FROM cycles
      WHERE :today BETWEEN start_date AND end_date;
      """)
  Mono<Cycle> findCurrentByDate(@Param("today") LocalDate today);
}
