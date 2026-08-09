package com.utp.users.mapper;

import com.utp.users.model.dto.UserLoginResponse;
import com.utp.users.model.dto.UserRegisterRequest;
import com.utp.users.model.dto.UserResponse;
import com.utp.users.model.entity.Campus;
import com.utp.users.model.entity.Role;
import com.utp.users.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = { CampusMapper.class, RoleMapper.class })
public interface UserMapper {

  @Mapping(target = "roles", source = "roles")
  @Mapping(target = "campus", source = "campus")
  UserResponse toUserResponse(User user, List<Role> roles, Campus campus);

  @Mapping(target = "roles", source = "roles")
  @Mapping(target = "campus", source = "campus")
  UserLoginResponse toUserLoginResponse(User user, List<Role> roles, Campus campus);

  @Mapping(target = "idUser", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "actualRegistered", ignore = true)
  User toEntity(UserRegisterRequest request);
}
