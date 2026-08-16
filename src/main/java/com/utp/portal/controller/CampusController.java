package com.utp.portal.controller;

import com.utp.portal.api.CampusApi;
import com.utp.portal.model.dto.CampusResponse;
import com.utp.portal.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class CampusController implements CampusApi {

  private final CampusService campusService;

  @Override
  public Mono<ResponseEntity<Flux<CampusResponse>>> getAllCampus(ServerWebExchange exchange) {
    return Mono.just(ResponseEntity.ok(campusService.findAll()));
  }

  @Override
  public Mono<ResponseEntity<CampusResponse>> getCampusById(Long id, ServerWebExchange exchange) {
    return campusService.findById(id).map(ResponseEntity::ok);
  }
}
