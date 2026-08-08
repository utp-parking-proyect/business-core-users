package com.utp.users.service.impl;

import com.utp.users.mapper.CampusMapperImpl;
import com.utp.users.mapper.RoleMapperImpl;
import com.utp.users.mapper.UserMapperImpl;
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
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

  private UserRepository userRepository;
  private UserRoleRepository userRoleRepository;
  private RoleRepository roleRepository;
  private CampusRepository campusRepository;
  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userRepository = Mockito.mock(UserRepository.class);
    userRoleRepository = Mockito.mock(UserRoleRepository.class);
    roleRepository = Mockito.mock(RoleRepository.class);
    campusRepository = Mockito.mock(CampusRepository.class);

    UserMapperImpl userMapper = new UserMapperImpl();
    ReflectionTestUtils.setField(userMapper, "campusMapper", new CampusMapperImpl());
    ReflectionTestUtils.setField(userMapper, "roleMapper", new RoleMapperImpl());

    userService = new UserServiceImpl(userRepository, userRoleRepository, roleRepository, campusRepository, userMapper);
  }

  private User sampleUser() {
    User user = new User();
    user.setIdUser(1L);
    user.setIdCampus(10L);
    user.setUsername("jdoe");
    user.setName("John");
    return user;
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
}
