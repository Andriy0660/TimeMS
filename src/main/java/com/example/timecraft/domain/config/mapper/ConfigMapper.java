package com.example.timecraft.domain.config.mapper;

import org.mapstruct.Mapper;

import com.example.timecraft.domain.config.dto.ConfigGetResponse;
import com.example.timecraft.domain.config.persistence.ConfigEntity;

@Mapper(componentModel = "spring")
public interface ConfigMapper {
  ConfigGetResponse fromEntity(final ConfigEntity entity);

}
