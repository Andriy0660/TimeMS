package com.example.timecraft.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Bad request")
public class BadRequestException extends RuntimeException {
  public BadRequestException(final String message) {
    super(message);
  }
  public BadRequestException() {}
}
