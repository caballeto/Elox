package com.lang.elox.errors;

import com.lang.elox.core.scanning.Token;

public final class RuntimeError extends RuntimeException {
  public final Token token;
  public final String message;

  public RuntimeError(Token token, String message) {
    super(message);
    this.message = message;
    this.token = token;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public Token getToken() {
    return token;
  }
}