package com.example.timecraft.domain.timelog.mapper;

import org.mapstruct.Mapper;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;

@Mapper(componentModel = "spring")
public interface TestTimeLogMapper {

  TimeLogEntity fromCreateResponse(final TimeLogCreateResponse response);

  TimeLogEntity fromUpdateResponse(final TimeLogUpdateResponse response);

  TimeLogUpdateRequest toUpdateRequest(final TimeLogEntity entity);

}