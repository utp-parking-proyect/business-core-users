package com.utp.portal.mapper;

import com.utp.portal.model.dto.CampusResponse;
import com.utp.portal.model.entity.Campus;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CampusMapper {

  CampusResponse toCampusResponse(Campus campus);
}
