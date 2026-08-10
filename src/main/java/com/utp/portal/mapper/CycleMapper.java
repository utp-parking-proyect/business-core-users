package com.utp.portal.mapper;

import com.utp.portal.model.dto.CycleResponse;
import com.utp.portal.model.entity.Cycle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CycleMapper {

  CycleResponse toCycleResponse(Cycle cycle);
}
