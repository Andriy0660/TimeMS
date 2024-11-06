package com.example.timecraft.domain.external_service.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.instancio.Instancio;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExternalTimeLogsApiTestUtils {
  public static int getSize(final MockMvc mvc, final LocalDate date) throws Exception {
    MvcResult resultBefore = mvc.perform(get("/external-time-logs")
            .param("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();
    return JsonPath.read(resultBefore.getResponse().getContentAsString(), "$.items.length()");
  }

  public static Matcher<?> matchExternalTimeLog(final ExternalTimeLogEntity entity) {
    List<Matcher<? super Map<String, String>>> matchers = new ArrayList<>();
    if (entity.getDate() != null) matchers.add(hasEntry("date", entity.getDate().toString()));
    if (entity.getStartTime() != null) matchers.add(hasEntry("startTime", entity.getStartTime().format(DateTimeFormatter.ISO_TIME)));
    if (entity.getEndTime() != null) matchers.add(hasEntry("endTime", entity.getEndTime().format(DateTimeFormatter.ISO_TIME)));
    if (entity.getDescription() != null) matchers.add(hasEntry("description", entity.getDescription()));

    return hasItem(allOf(matchers));
  }

  public static ExternalTimeLogCreateFromTimeLogRequest getCreateRequest(final LocalDate date) {
    final ExternalTimeLogCreateFromTimeLogRequest request1 = Instancio.of(ExternalTimeLogCreateFromTimeLogRequest.class)
        .set(field(ExternalTimeLogCreateFromTimeLogRequest::getDate), date)
        .create();
    request1.setStartTime(request1.getStartTime().withNano(0));
    request1.setEndTime(request1.getEndTime().withNano(0));
    return request1;
  }

}
