package com.utp.portal.controller;

import com.utp.portal.api.CyclesApi;
import com.utp.portal.model.dto.CycleResponse;
import com.utp.portal.service.CycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class CycleController implements CyclesApi {

  private final CycleService cycleService;

  @Override
  public Mono<ResponseEntity<CycleResponse>> getCurrentCycle(ServerWebExchange exchange) {
    return cycleService.getCurrentCycle().map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CycleResponse>> getCycleById(Long id, ServerWebExchange exchange) {
    return cycleService.findById(id).map(ResponseEntity::ok);
  }
}
