package com.utp.portal.service.impl;

import com.utp.portal.mapper.CycleMapper;
import com.utp.portal.model.dto.CycleResponse;
import com.utp.portal.repository.CycleRepository;
import com.utp.portal.service.CycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class CycleServiceImpl implements CycleService {

  private final CycleRepository cycleRepository;
  private final CycleMapper cycleMapper;

  @Override
  public Mono<CycleResponse> getCurrentCycle() {
    return cycleRepository.findCurrentByDate(LocalDate.now())
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No existe un ciclo vigente para la fecha actual")))
        .map(cycleMapper::toCycleResponse);
  }

  @Override
  public Mono<CycleResponse> findById(Long id) {
    return cycleRepository.findById(id)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No existe un ciclo con id " + id)))
        .map(cycleMapper::toCycleResponse);
  }
}
