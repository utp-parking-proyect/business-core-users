package com.utp.users.service.impl;

import com.utp.users.mapper.UserMapper;
import com.utp.users.model.dto.UserLoginResponse;
import com.utp.users.model.dto.UserPageResponse;
import com.utp.users.model.dto.UserResponse;
import com.utp.users.model.entity.Campus;
import com.utp.users.model.entity.Role;
import com.utp.users.model.entity.User;
import com.utp.users.repository.CampusRepository;
import com.utp.users.repository.RoleRepository;
import com.utp.users.repository.UserRepository;
import com.utp.users.repository.UserRoleRepository;
import com.utp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final RoleRepository roleRepository;
  private final CampusRepository campusRepository;
  private final UserMapper userMapper;

  @Override
  public Mono<UserPageResponse> findAll(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);

    Mono<List<UserResponse>> content = userRepository.findAllBy(pageable)
        .concatMap(user -> rolesAndCampus(user)
            .map(tuple -> userMapper
                .toUserResponse(user, tuple.getT1(), tuple.getT2().orElse(null))))
        .collectList();

    return Mono.zip(content, userRepository.count())
        .map(tuple -> {
          long totalElements = tuple.getT2();
          int totalPages = (int) Math.ceil((double) totalElements / size);
          return UserPageResponse.builder()
              .content(tuple.getT1())
              .page(page)
              .size(size)
              .totalElements(totalElements)
              .totalPages(totalPages)
              .build();
        });
  }

  @Override
  public Mono<UserResponse> findById(Long id) {
    return userRepository.findById(id)
        .flatMap(user -> rolesAndCampus(user)
            .map(tuple -> userMapper
                .toUserResponse(user, tuple.getT1(), tuple.getT2().orElse(null))));
  }

  @Override
  public Mono<UserLoginResponse> findByUsername(String username) {
    return userRepository.findByUsername(username)
        .flatMap(user -> rolesAndCampus(user)
            .map(tuple -> userMapper
                .toUserLoginResponse(user, tuple.getT1(), tuple.getT2().orElse(null))));
  }

  private Mono<Tuple2<List<Role>, Optional<Campus>>> rolesAndCampus(User user) {
    return Mono.zip(findRoles(user.getIdUser()), findCampus(user.getIdCampus()));
  }

  private Mono<List<Role>> findRoles(Long idUser) {
    return userRoleRepository.findAllByIdUser(idUser)
        .flatMap(userRole -> roleRepository.findById(userRole.getIdRole()))
        .collectList();
  }

  private Mono<Optional<Campus>> findCampus(Long idCampus) {
    return Mono.justOrEmpty(idCampus)
        .flatMap(campusRepository::findById)
        .map(Optional::of)
        .defaultIfEmpty(Optional.empty());
  }
}
