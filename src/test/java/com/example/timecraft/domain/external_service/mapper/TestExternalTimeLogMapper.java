package com.example.timecraft.domain.external_service.mapper;

import org.mapstruct.Mapper;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogResponse;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;

@Mapper(componentModel = "spring")
public interface TestExternalTimeLogMapper {

  ExternalTimeLogEntity fromCreateRequest(final ExternalTimeLogCreateFromTimeLogRequest request);

  ExternalTimeLogEntity fromCreateResponse(final ExternalTimeLogCreateFromTimeLogResponse response);

}
