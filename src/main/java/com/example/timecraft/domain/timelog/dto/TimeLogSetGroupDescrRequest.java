package com.example.timecraft.domain.timelog.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeLogSetGroupDescrRequest {
  private List<Long> ids;
  private String description;
}
