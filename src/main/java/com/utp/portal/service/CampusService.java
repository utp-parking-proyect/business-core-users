package com.utp.portal.service;

import com.utp.portal.model.dto.CampusResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CampusService {
  Flux<CampusResponse> findAll();
  Mono<CampusResponse> findById(Long id);
}
