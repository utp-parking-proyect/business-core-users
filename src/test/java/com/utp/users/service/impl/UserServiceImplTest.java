package com.utp.users.service.impl;

import com.utp.users.mapper.CampusMapperImpl;
import com.utp.users.mapper.RoleMapperImpl;
import com.utp.users.mapper.UserMapperImpl;
import com.utp.users.model.dto.UserRegisterRequest;
import com.utp.users.model.entity.Campus;
import com.utp.users.model.entity.Role;
import com.utp.users.model.entity.User;
import com.utp.users.model.entity.UserRole;
import com.utp.users.repository.CampusRepository;
import com.utp.users.repository.RoleRepository;
import com.utp.users.repository.UserRepository;
import com.utp.users.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

  private UserRepository userRepository;
  private UserRoleRepository userRoleRepository;
  private RoleRepository roleRepository;
  private CampusRepository campusRepository;
  private PasswordEncoder passwordEncoder;
  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userRepository = Mockito.mock(UserRepository.class);
    userRoleRepository = Mockito.mock(UserRoleRepository.class);
    roleRepository = Mockito.mock(RoleRepository.class);
    campusRepository = Mockito.mock(CampusRepository.class);
    passwordEncoder = Mockito.mock(PasswordEncoder.class);

    UserMapperImpl userMapper = new UserMapperImpl();
    ReflectionTestUtils.setField(userMapper, "campusMapper", new CampusMapperImpl());
    ReflectionTestUtils.setField(userMapper, "roleMapper", new RoleMapperImpl());

    userService = new UserServiceImpl(userRepository, userRoleRepository, roleRepository, campusRepository,
        userMapper, passwordEncoder);
  }

  private User sampleUser() {
    User user = new User();
    user.setIdUser(1L);
    user.setIdCampus(10L);
    user.setUsername("jdoe");
    user.setName("John");
    return user;
  }

  private UserRegisterRequest sampleRegisterRequest() {
    return UserRegisterRequest.builder()
        .username("jdoe")
        .password("plain-password")
        .name("John")
        .lastname("Doe")
        .dni("12345678")
        .institutionalEmail("jdoe@utp.edu.pe")
        .career("Systems Engineering")
        .idCampus(10L)
        .build();
  }

  @Test
  void findById_mapsRolesAndCampus_whenBothPresent() {
    User user = sampleUser();
    UserRole userRole = new UserRole(1L, 100L, user.getIdUser());
    Role role = new Role(100L, "ADMIN");
    Campus campus = new Campus(10L, "Main Campus");

    when(userRepository.findById(1L)).thenReturn(Mono.just(user));
    when(userRoleRepository.findAllByIdUser(1L)).thenReturn(Flux.just(userRole));
    when(roleRepository.findById(100L)).thenReturn(Mono.just(role));
    when(campusRepository.findById(10L)).thenReturn(Mono.just(campus));

    StepVerifier.create(userService.findById(1L))
        .assertNext(dto -> {
          assert dto.getRoles().size() == 1;
          assert dto.getRoles().get(0).getName().equals("ADMIN");
          assert dto.getCampus() != null;
          assert dto.getCampus().getNameCampus().equals("Main Campus");
        })
        .verifyComplete();
  }

  @Test
  void findById_stillReturnsUser_whenCampusLookupIsEmpty() {
    // Regression test: Mono.zip previously dropped the whole user silently when the
    // campus Mono completed empty (e.g. orphaned/missing id_campus). It must now come
    // back with campus == null instead of the user vanishing from the response.
    User user = sampleUser();

    when(userRepository.findById(1L)).thenReturn(Mono.just(user));
    when(userRoleRepository.findAllByIdUser(1L)).thenReturn(Flux.empty());
    when(campusRepository.findById(10L)).thenReturn(Mono.empty());

    StepVerifier.create(userService.findById(1L))
        .assertNext(dto -> {
          assert dto.getIdUser().equals(1L);
          assert dto.getCampus() == null;
          assert dto.getRoles().isEmpty();
        })
        .verifyComplete();
  }

  @Test
  void findById_stillReturnsUser_whenIdCampusIsNull() {
    User user = sampleUser();
    user.setIdCampus(null);

    when(userRepository.findById(1L)).thenReturn(Mono.just(user));
    when(userRoleRepository.findAllByIdUser(1L)).thenReturn(Flux.empty());

    StepVerifier.create(userService.findById(1L))
        .assertNext(dto -> {
          assert dto.getCampus() == null;
        })
        .verifyComplete();

    Mockito.verify(campusRepository, Mockito.never()).findById(anyLong());
  }

  @Test
  void findById_completesEmpty_whenUserNotFound() {
    when(userRepository.findById(99L)).thenReturn(Mono.empty());

    StepVerifier.create(userService.findById(99L))
        .verifyComplete();
  }

  @Test
  void findAll_returnsPagedContentWithTotals() {
    User user = sampleUser();

    when(userRepository.findAllBy(PageRequest.of(0, 20))).thenReturn(Flux.just(user));
    when(userRoleRepository.findAllByIdUser(1L)).thenReturn(Flux.empty());
    when(campusRepository.findById(10L)).thenReturn(Mono.empty());
    when(userRepository.count()).thenReturn(Mono.just(1L));

    StepVerifier.create(userService.findAll(0, 20))
        .assertNext(pageResponse -> {
          assert pageResponse.getContent().size() == 1;
          assert pageResponse.getPage() == 0;
          assert pageResponse.getSize() == 20;
          assert pageResponse.getTotalElements() == 1L;
          assert pageResponse.getTotalPages() == 1;
        })
        .verifyComplete();
  }

  @Test
  void findAll_computesTotalPagesAcrossMultiplePages() {
    // 25 total users at 10 per page must round up to 3 pages, not truncate to 2.
    when(userRepository.findAllBy(PageRequest.of(1, 10))).thenReturn(Flux.empty());
    when(userRepository.count()).thenReturn(Mono.just(25L));

    StepVerifier.create(userService.findAll(1, 10))
        .assertNext(pageResponse -> {
          assert pageResponse.getTotalPages() == 3;
        })
        .verifyComplete();
  }

  @Test
  void register_savesHashedPassword_andReturnsUserWithoutIt() {
    Campus campus = new Campus(10L, "Main Campus");
    UserRegisterRequest request = sampleRegisterRequest();

    when(campusRepository.findById(10L)).thenReturn(Mono.just(campus));
    when(userRepository.existsByUsername("jdoe")).thenReturn(Mono.just(false));
    when(userRepository.existsByDni("12345678")).thenReturn(Mono.just(false));
    when(userRepository.existsByInstitutionalEmail("jdoe@utp.edu.pe")).thenReturn(Mono.just(false));
    when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User saved = invocation.getArgument(0);
      saved.setIdUser(1L);
      return Mono.just(saved);
    });

    ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);

    StepVerifier.create(userService.register(request))
        .assertNext(dto -> {
          assert dto.getIdUser().equals(1L);
          assert dto.getUsername().equals("jdoe");
          assert dto.getCampus().getNameCampus().equals("Main Campus");
          assert dto.getRoles().isEmpty();
        })
        .verifyComplete();

    Mockito.verify(userRepository).save(savedUserCaptor.capture());
    assert savedUserCaptor.getValue().getPassword().equals("hashed-password");
  }

  @Test
  void register_fails404_whenCampusDoesNotExist() {
    UserRegisterRequest request = sampleRegisterRequest();

    when(campusRepository.findById(10L)).thenReturn(Mono.empty());
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDni(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByInstitutionalEmail(anyString())).thenReturn(Mono.just(false));

    StepVerifier.create(userService.register(request))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 404)
        .verify();

    Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
  }

  @Test
  void register_fails409_whenUsernameAlreadyExists() {
    Campus campus = new Campus(10L, "Main Campus");
    UserRegisterRequest request = sampleRegisterRequest();

    when(campusRepository.findById(10L)).thenReturn(Mono.just(campus));
    when(userRepository.existsByUsername("jdoe")).thenReturn(Mono.just(true));
    when(userRepository.existsByDni(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByInstitutionalEmail(anyString())).thenReturn(Mono.just(false));

    StepVerifier.create(userService.register(request))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 409)
        .verify();

    Mockito.verify(userRepository, Mockito.never()).save(any(User.class));
  }

  @Test
  void register_fails409_whenDniAlreadyExists() {
    Campus campus = new Campus(10L, "Main Campus");
    UserRegisterRequest request = sampleRegisterRequest();

    when(campusRepository.findById(10L)).thenReturn(Mono.just(campus));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDni("12345678")).thenReturn(Mono.just(true));
    when(userRepository.existsByInstitutionalEmail(anyString())).thenReturn(Mono.just(false));

    StepVerifier.create(userService.register(request))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 409)
        .verify();
  }

  @Test
  void register_fails409_whenInstitutionalEmailAlreadyExists() {
    Campus campus = new Campus(10L, "Main Campus");
    UserRegisterRequest request = sampleRegisterRequest();

    when(campusRepository.findById(10L)).thenReturn(Mono.just(campus));
    when(userRepository.existsByUsername(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByDni(anyString())).thenReturn(Mono.just(false));
    when(userRepository.existsByInstitutionalEmail("jdoe@utp.edu.pe")).thenReturn(Mono.just(true));

    StepVerifier.create(userService.register(request))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 409)
        .verify();
  }

  @Test
  void assignRole_savesNewAssignment_whenNotAlreadyAssigned() {
    User user = sampleUser();
    Role role = new Role(100L, "ADMIN");

    when(userRepository.findById(1L)).thenReturn(Mono.just(user));
    when(roleRepository.findById(100L)).thenReturn(Mono.just(role));
    when(userRoleRepository.existsByIdUserAndIdRole(1L, 100L)).thenReturn(Mono.just(false));
    when(userRoleRepository.save(any(UserRole.class))).thenReturn(Mono.just(new UserRole(1L, 100L, 1L)));
    when(userRoleRepository.findAllByIdUser(1L)).thenReturn(Flux.just(new UserRole(1L, 100L, 1L)));
    when(campusRepository.findById(10L)).thenReturn(Mono.empty());

    StepVerifier.create(userService.assignRole(1L, 100L))
        .assertNext(dto -> {
          assert dto.getRoles().size() == 1;
          assert dto.getRoles().get(0).getName().equals("ADMIN");
        })
        .verifyComplete();

    ArgumentCaptor<UserRole> savedCaptor = ArgumentCaptor.forClass(UserRole.class);
    Mockito.verify(userRoleRepository).save(savedCaptor.capture());
    assert savedCaptor.getValue().getIdUser().equals(1L);
    assert savedCaptor.getValue().getIdRole().equals(100L);
  }

  @Test
  void assignRole_isIdempotent_whenAlreadyAssigned() {
    User user = sampleUser();
    Role role = new Role(100L, "ADMIN");

    when(userRepository.findById(1L)).thenReturn(Mono.just(user));
    when(roleRepository.findById(100L)).thenReturn(Mono.just(role));
    when(userRoleRepository.existsByIdUserAndIdRole(1L, 100L)).thenReturn(Mono.just(true));
    when(userRoleRepository.findAllByIdUser(1L)).thenReturn(Flux.just(new UserRole(1L, 100L, 1L)));
    when(campusRepository.findById(10L)).thenReturn(Mono.empty());

    StepVerifier.create(userService.assignRole(1L, 100L))
        .assertNext(dto -> {
          assert dto.getRoles().size() == 1;
        })
        .verifyComplete();

    Mockito.verify(userRoleRepository, Mockito.never()).save(any(UserRole.class));
  }

  @Test
  void assignRole_fails404_whenUserDoesNotExist() {
    when(userRepository.findById(1L)).thenReturn(Mono.empty());
    when(roleRepository.findById(100L)).thenReturn(Mono.just(new Role(100L, "ADMIN")));

    StepVerifier.create(userService.assignRole(1L, 100L))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 404)
        .verify();
  }

  @Test
  void assignRole_fails404_whenRoleDoesNotExist() {
    when(userRepository.findById(1L)).thenReturn(Mono.just(sampleUser()));
    when(roleRepository.findById(100L)).thenReturn(Mono.empty());

    StepVerifier.create(userService.assignRole(1L, 100L))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 404)
        .verify();
  }

  @Test
  void unassignRole_deletesAssignment_andReturnsUpdatedUser() {
    User user = sampleUser();

    when(userRepository.findById(1L)).thenReturn(Mono.just(user));
    when(userRoleRepository.deleteByIdUserAndIdRole(1L, 100L)).thenReturn(Mono.empty());
    when(userRoleRepository.findAllByIdUser(1L)).thenReturn(Flux.empty());
    when(campusRepository.findById(10L)).thenReturn(Mono.empty());

    StepVerifier.create(userService.unassignRole(1L, 100L))
        .assertNext(dto -> {
          assert dto.getRoles().isEmpty();
        })
        .verifyComplete();

    Mockito.verify(userRoleRepository).deleteByIdUserAndIdRole(1L, 100L);
  }

  @Test
  void unassignRole_fails404_whenUserDoesNotExist() {
    when(userRepository.findById(1L)).thenReturn(Mono.empty());

    StepVerifier.create(userService.unassignRole(1L, 100L))
        .expectErrorMatches(error -> error instanceof ResponseStatusException responseStatusException
            && responseStatusException.getStatusCode().value() == 404)
        .verify();

    Mockito.verify(userRoleRepository, Mockito.never()).deleteByIdUserAndIdRole(anyLong(), anyLong());
  }
}
