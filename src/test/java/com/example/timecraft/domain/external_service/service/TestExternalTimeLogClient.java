package com.example.timecraft.domain.external_service.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.external_service.mapper.TestExternalTimeLogMapper;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogResponse;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
@RequiredArgsConstructor
public class TestExternalTimeLogClient {
  private final ObjectMapper objectMapper;
  private final TestExternalTimeLogMapper externalTimeLogMapper;
  private final MockMvc mvc;

  public ExternalTimeLogEntity saveExternalTimeLog(final ExternalTimeLogCreateFromTimeLogRequest request, final String accessToken) throws Exception {
    final MvcResult result = mvc.perform(post("/external-time-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk()).andReturn();

    final String content = result.getResponse().getContentAsString();
    final ExternalTimeLogCreateFromTimeLogResponse response = objectMapper.readValue(content, ExternalTimeLogCreateFromTimeLogResponse.class);
    return externalTimeLogMapper.fromCreateResponse(response);
  }
}
