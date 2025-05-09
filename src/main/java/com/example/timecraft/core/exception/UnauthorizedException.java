package com.example.timecraft.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(final String message) {
    super(message);
  }

  public UnauthorizedException() {}
}
