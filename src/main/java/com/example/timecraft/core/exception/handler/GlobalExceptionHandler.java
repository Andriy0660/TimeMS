package com.example.timecraft.core.exception.handler;

import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(RuntimeException.class)
  public ResponseStatusException handleRuntimeException(final RuntimeException e, final WebRequest request) {
    ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
    if (responseStatus != null) {
      final String message = Optional.ofNullable(e.getMessage()).orElse(responseStatus.reason());
      return new ResponseStatusException(responseStatus.code(), message);
    } else {
      log.error("Error occurred when processing request: {}", extractRequestInfo(request), e);
      return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String extractRequestInfo(final WebRequest webRequest) {
    if (webRequest instanceof final ServletWebRequest servletWebRequest) {
      final HttpServletRequest request = servletWebRequest.getRequest();
      final String method = request.getMethod();
      final String contextPath = servletWebRequest.getContextPath();
      final String servletPath = request.getServletPath();
      return method + " " + contextPath + servletPath;
    }
    return "<Unknown>";
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseStatusException handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseStatusException handleMissingParameter(final MissingServletRequestParameterException e) {
    String parameterName = e.getParameterName();
    String errorMessage = "Required parameter is missing: " + parameterName;

    return new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseStatusException handleInvalidArgumentType(final MethodArgumentTypeMismatchException e) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid argument type of parameter: " + e.getName());
  }
}


