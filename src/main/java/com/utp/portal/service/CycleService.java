package com.utp.portal.service;

import com.utp.portal.model.dto.CycleResponse;
import reactor.core.publisher.Mono;

public interface CycleService {
  Mono<CycleResponse> getCurrentCycle();
  Mono<CycleResponse> findById(Long id);
}
