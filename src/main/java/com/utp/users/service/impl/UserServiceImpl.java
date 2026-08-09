package com.utp.users.service.impl;

import com.utp.users.mapper.UserMapper;
import com.utp.users.model.dto.UserLoginResponse;
import com.utp.users.model.dto.UserPageResponse;
import com.utp.users.model.dto.UserRegisterRequest;
import com.utp.users.model.dto.UserResponse;
import com.utp.users.model.entity.Campus;
import com.utp.users.model.entity.Role;
import com.utp.users.model.entity.User;
import com.utp.users.model.entity.UserRole;
import com.utp.users.repository.CampusRepository;
import com.utp.users.repository.RoleRepository;
import com.utp.users.repository.UserRepository;
import com.utp.users.repository.UserRoleRepository;
import com.utp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
  private final PasswordEncoder passwordEncoder;

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
    return userRepository.findById(id).flatMap(this::toUserResponse);
  }

  @Override
  public Mono<UserLoginResponse> findByUsername(String username) {
    return userRepository.findByUsername(username)
        .flatMap(user -> rolesAndCampus(user)
            .map(tuple -> userMapper
                .toUserLoginResponse(user, tuple.getT1(), tuple.getT2().orElse(null))));
  }

  private Mono<UserResponse> toUserResponse(User user) {
    return rolesAndCampus(user)
        .map(tuple -> userMapper.toUserResponse(user, tuple.getT1(), tuple.getT2().orElse(null)));
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

  @Override
  public Mono<UserResponse> register(UserRegisterRequest request) {
    Mono<Campus> campusMono = campusRepository.findById(request.getIdCampus())
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No existe un campus con id " + request.getIdCampus())));

    return campusMono.flatMap(campus ->
        ensureUnique(userRepository.existsByUsername(request.getUsername()),
            "El username ya está registrado")
            .then(ensureUnique(userRepository.existsByDni(request.getDni()),
                "El dni ya está registrado"))
            .then(ensureUnique(userRepository.existsByInstitutionalEmail(request.getInstitutionalEmail()),
                "El institutionalEmail ya está registrado"))
            .then(Mono.defer(() -> {
              User user = userMapper.toEntity(request);
              user.setPassword(passwordEncoder.encode(request.getPassword()));
              user.setActualRegistered(true);
              return userRepository.save(user);
            }))
            .map(savedUser -> userMapper.toUserResponse(savedUser, List.of(), campus)));
  }

  private Mono<Void> ensureUnique(Mono<Boolean> existsMono, String message) {
    return existsMono.flatMap(exists -> exists
        ? Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, message))
        : Mono.empty());
  }

  @Override
  public Mono<UserResponse> assignRole(Long idUser, Long idRole) {
    Mono<User> userMono = userRepository.findById(idUser)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No existe un usuario con id " + idUser)));
    Mono<Role> roleMono = roleRepository.findById(idRole)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No existe un rol con id " + idRole)));

    return Mono.zip(userMono, roleMono)
        .flatMap(tuple -> userRoleRepository.existsByIdUserAndIdRole(idUser, idRole)
            .flatMap(alreadyAssigned -> alreadyAssigned
                ? Mono.empty()
                : userRoleRepository.save(new UserRole(null, idRole, idUser)))
            .then(toUserResponse(tuple.getT1())));
  }

  @Override
  public Mono<UserResponse> unassignRole(Long idUser, Long idRole) {
    return userRepository.findById(idUser)
        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
            "No existe un usuario con id " + idUser)))
        .flatMap(user -> userRoleRepository.deleteByIdUserAndIdRole(idUser, idRole)
            .then(toUserResponse(user)));
  }
}
