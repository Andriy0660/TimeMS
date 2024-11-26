package com.example.timecraft.domain.jira.worklog.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.atlassian.adf.model.node.Doc;
import com.atlassian.adf.model.node.Paragraph;
import com.fasterxml.jackson.databind.JsonNode;

import static com.atlassian.adf.model.node.Doc.doc;
import static com.atlassian.adf.model.node.Paragraph.p;

public class JiraWorklogUtils {
  public static final DateTimeFormatter JIRA_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  public static String getAuthorizationHeader(final String email, final String token) {
    final String auth = email + ":" + token;
    return Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
  }

  public static String getTextFromAdf(final JsonNode node) {
    final StringBuilder text = new StringBuilder();

    if (node.has("content")) {
      for (JsonNode content : node.get("content")) {
        String type = content.get("type").asText();

        switch (type) {
          case "hardBreak":
            text.append("\n");
            break;
          case "paragraph":
            if (!text.isEmpty()) {
              text.append("\n");
            }
            break;
          case "listItem":
            text.append("\n- ");
            break;
          case "text":
            String textContent = content.get("text").asText();
            if (content.has("marks")) {
              for (JsonNode mark : content.get("marks")) {
                if (mark.get("type").asText().equals("code")) {
                  textContent = "`" + textContent + "`";
                  break;
                }
              }
            }
            text.append(textContent);
            break;
          default:
            break;
        }

        if (content.has("content")) {
          text.append(getTextFromAdf(content));
        }
      }
    }
    return text.toString().trim();
  }

  public static String getJiraStartedTime(final LocalDateTime dateTime) {
    return dateTime.atZone(ZoneId.systemDefault()).format(JIRA_DATE_TIME_FORMATTER);
  }

  public static Map<String, ?> getJiraComment(final String description) {
    List<String> lines;
    if (description != null) {
      lines = description.lines().toList();
    } else {
      lines = Collections.emptyList();
    }
    final List<Paragraph> paragraphs = new ArrayList<>();

    for (String line : lines) {
      if (!line.isEmpty()) {
        paragraphs.add(p(line));
      } else {
        paragraphs.add(p());
      }
    }
    Doc commentDoc = doc(paragraphs);
    return commentDoc.toMap();
  }
}
