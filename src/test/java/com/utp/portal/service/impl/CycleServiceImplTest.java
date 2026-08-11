package com.utp.portal.service.impl;

import com.utp.portal.mapper.CycleMapperImpl;
import com.utp.portal.model.entity.Cycle;
import com.utp.portal.repository.CycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CycleServiceImplTest {

  private CycleRepository cycleRepository;
  private CycleServiceImpl cycleService;

  @BeforeEach
  void setUp() {
    cycleRepository = Mockito.mock(CycleRepository.class);
    cycleService = new CycleServiceImpl(cycleRepository, new CycleMapperImpl());
  }

  private Cycle cycle(Long idCycle, String nameCycle, LocalDate startDate, LocalDate endDate) {
    return new Cycle(idCycle, nameCycle, startDate, endDate);
  }

  @Test
  void getCurrentCycle_returnsCycleForToday() {
    when(cycleRepository.findCurrentByDate(any(LocalDate.class)))
        .thenReturn(Mono.just(cycle(2L, "2026-2", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 31))));

    StepVerifier.create(cycleService.getCurrentCycle())
        .assertNext(dto -> {
          assert dto.getIdCycle().equals(2L);
          assert dto.getNameCycle().equals("2026-2");
        })
        .verifyComplete();
  }

  @Test
  void getCurrentCycle_fails404_whenNoCycleMatchesToday() {
    when(cycleRepository.findCurrentByDate(any(LocalDate.class))).thenReturn(Mono.empty());

    StepVerifier.create(cycleService.getCurrentCycle())
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 404)
        .verify();
  }

  @Test
  void findById_returnsCycle_whenExists() {
    when(cycleRepository.findById(2L))
        .thenReturn(Mono.just(cycle(2L, "2026-2", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 31))));

    StepVerifier.create(cycleService.findById(2L))
        .assertNext(dto -> {
          assert dto.getIdCycle().equals(2L);
        })
        .verifyComplete();
  }

  @Test
  void findById_fails404_whenNotFound() {
    when(cycleRepository.findById(99L)).thenReturn(Mono.empty());

    StepVerifier.create(cycleService.findById(99L))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 404)
        .verify();
  }
}
