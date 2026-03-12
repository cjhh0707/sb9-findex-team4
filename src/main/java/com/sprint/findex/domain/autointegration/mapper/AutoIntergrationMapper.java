package com.sprint.findex.domain.autointegration.mapper;

import com.sprint.findex.domain.autointegration.dto.AutoIntegrationDto;
import com.sprint.findex.domain.autointegration.entity.AutoIntegration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// 엔티티 <--> DTO 변환 담당(서비스에서 컨트롤러로 데이터 안전하게 전달)
@Mapper(componentModel = "spring")
public interface AutoIntergrationMapper {

//  엔티티 -> DTO

  AutoIntegrationDto toDto(AutoIntegration entity);
//  DTO -> 엔티티
  @Mapping(target = "lastSyncAt", ignore = true)
  AutoIntegration toEntity(AutoIntegrationDto dto);


}
