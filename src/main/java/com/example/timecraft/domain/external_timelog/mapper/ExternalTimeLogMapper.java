package com.example.timecraft.domain.external_timelog.mapper;

import org.mapstruct.Mapper;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogResponse;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;

@Mapper(componentModel = "spring")
public interface ExternalTimeLogMapper {
  ExternalTimeLogListResponse.ExternalTimeLogDto toListItem(final ExternalTimeLogEntity entity);

  ExternalTimeLogCreateFromTimeLogResponse toCreateResponse(final ExternalTimeLogEntity entity);

  ExternalTimeLogEntity fromCreateFromTimeLogRequest(final ExternalTimeLogCreateFromTimeLogRequest request);

}