package com.example.timecraft.domain.timelog.dto;

import java.util.List;

import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogListResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogImportAllRequest {
    private List<TimeLogListResponse.TimeLogDto> timeLogs;
    private List<WorklogListResponse.WorklogDto> worklogs;
    private List<ExternalTimeLogListResponse.ExternalTimeLogDto> externalTimeLogs;
}