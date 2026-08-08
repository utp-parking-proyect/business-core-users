package com.utp.users.mapper;

import com.utp.users.model.dto.CampusResponse;
import com.utp.users.model.entity.Campus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CampusMapper {

  CampusResponse toCampusResponse(Campus campus);
}
