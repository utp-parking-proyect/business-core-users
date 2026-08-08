package com.utp.users.mapper;

import com.utp.users.model.dto.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Maps the persistence entity {@link com.utp.users.model.entity.Role} to the API model
 * {@link com.utp.users.model.dto.Role}. Both share the simple name "Role" by design (the API
 * model deliberately drops the "Dto" suffix), so this interface refers to each by its
 * fully-qualified name instead of importing both under the same simple name.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {

  Role toRoleResponse(com.utp.users.model.entity.Role role);
}
