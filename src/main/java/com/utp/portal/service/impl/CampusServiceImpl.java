package com.utp.portal.service.impl;

import com.utp.portal.mapper.CampusMapper;
import com.utp.portal.model.dto.CampusResponse;
import com.utp.portal.repository.CampusRepository;
import com.utp.portal.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CampusServiceImpl implements CampusService {

  private final CampusRepository campusRepository;
  private final CampusMapper campusMapper;

  @Override
  public Flux<CampusResponse> findAll() {
    return campusRepository.findAll().map(campusMapper::toCampusResponse);
  }

  @Override
  public Mono<CampusResponse> findById(Long id) {
    return campusRepository.findById(id)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No existe un campus con id " + id)))
        .map(campusMapper::toCampusResponse);
  }
}
