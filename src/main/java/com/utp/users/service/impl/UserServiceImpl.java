package com.utp.users.service.impl;

import com.utp.users.model.dto.UserDto;
import com.utp.users.model.dto.UserLoginDto;
import com.utp.users.model.entity.Role;
import com.utp.users.model.entity.User;
import com.utp.users.repository.RoleRepository;
import com.utp.users.repository.UserRepository;
import com.utp.users.repository.UserRoleRepository;
import com.utp.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public Flux<UserDto> findAll() {
        return userRepository.findAll()
                .concatMap(user ->
                        userRoleRepository.findAllByIdUser(user.getIdUser())
                                .flatMap(userRole -> roleRepository.findById(userRole.getIdRole()))
                                .collectList()
                                .switchIfEmpty(Mono.just(Collections.emptyList()))
                                .map(roles -> userToUserDto(user, roles))
                );
    }

    @Override
    public Mono<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .flatMap(user ->
                        userRoleRepository.findAllByIdUser(id)
                                .flatMap(userRole -> roleRepository.findById(userRole.getIdRole()))
                                .collectList()
                                .map(roles -> userToUserDto(user, roles))
                );
    }

    @Override
    public Mono<UserLoginDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user ->
                        userRoleRepository.findAllByIdUser(user.getIdUser())
                                .flatMap(userRole -> roleRepository.findById(userRole.getIdRole()))
                                .collectList()
                                .map(roles -> userToUserLoginDto(user, roles))
                );
    }

    private UserDto userToUserDto(User user, List<Role> roles) {
        return UserDto.builder()
                .idUser(user.getIdUser())
                .username(user.getUsername())
                .name(user.getName())
                .lastname(user.getLastname())
                .dni(user.getDni())
                .institutionalEmail(user.getInstitutionalEmail())
                .career(user.getCareer())
                .actualRegistered(user.getActualRegistered())
                .roles(roles != null ? roles : Collections.emptyList())
                .build();
    }

    private UserLoginDto userToUserLoginDto(User user, List<Role> roles) {
        return UserLoginDto.builder()
                .idUser(user.getIdUser())
                .username(user.getUsername())
                .password(user.getPassword())
                .name(user.getName())
                .lastname(user.getLastname())
                .dni(user.getDni())
                .institutionalEmail(user.getInstitutionalEmail())
                .career(user.getCareer())
                .actualRegistered(user.getActualRegistered())
                .roles(roles != null ? roles : Collections.emptyList())
                .build();
    }
}
