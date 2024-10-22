package com.example.timecraft.domain.timelog.service;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
@RequiredArgsConstructor
public class TestTimeLogService {
  private final ObjectMapper objectMapper;
  private final TimeLogMapper timeLogMapper;
  private final MockMvc mvc;

  public TimeLogEntity saveTimeLog(final TimeLogCreateRequest request) throws Exception {
    final LocalTime startTime = request.getStartTime();
    return saveTimeLog(request, startTime != null ? startTime.plusHours(1) : null);
  }

  public TimeLogEntity saveTimeLog(final TimeLogCreateRequest request, final LocalTime endTime) throws Exception {
    final MvcResult result = mvc.perform(post("/time-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    final String content = result.getResponse().getContentAsString();
    final TimeLogCreateResponse response = objectMapper.readValue(content, TimeLogCreateResponse.class);
    final TimeLogEntity entity = timeLogMapper.fromCreateResponse(response);

    if (endTime != null) {
      final TimeLogUpdateRequest updateRequest = timeLogMapper.toUpdateRequest(entity);
      updateRequest.setEndTime(endTime);
      final MvcResult updateResult = mvc.perform(put("/time-logs/{id}", entity.getId())
              .content(objectMapper.writeValueAsString(updateRequest))
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk()).andReturn();

      final String updateContent = updateResult.getResponse().getContentAsString();
      final TimeLogUpdateResponse updateResponse = objectMapper.readValue(updateContent, TimeLogUpdateResponse.class);
      return timeLogMapper.fromUpdateResponse(updateResponse);
    }

    return entity;
  }
}
