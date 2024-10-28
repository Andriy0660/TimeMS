package com.example.timecraft.domain.worklog.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.mapper.TestWorklogMapper;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.util.WorklogApiTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.RequiredArgsConstructor;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
@RequiredArgsConstructor
public class TestWorklogClient {
  private final WireMockServer wm;
  private final ObjectMapper objectMapper;
  private final TestWorklogMapper worklogMapper;
  private final MockMvc mvc;

  public WorklogEntity saveWorklog(final WorklogCreateFromTimeLogRequest request) throws Exception {
    wm.stubFor(WireMock.post(WireMock.urlMatching(".*/issue/" + request.getTicket() + "/worklog"))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(created()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(WorklogApiTestUtils.generateWorklogResponseBody(request, objectMapper))
        )
    );

    final MvcResult result = mvc.perform(post("/work-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    final String content = result.getResponse().getContentAsString();
    final WorklogCreateFromTimeLogResponse response = objectMapper.readValue(content, WorklogCreateFromTimeLogResponse.class);
    return worklogMapper.fromCreateResponse(response);
  }
}
