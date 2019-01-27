package com.lang.elox.representation.runtime.instances;

import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;

public final class TException extends RuntimeException {
  private final Object value;
  private final Token token;

  public TException(Token token, Object value) {
    super((String) token.literal);
    this.token = token;
    this.value = value;
  }

  public TException(RuntimeError e) {
    super(e.getMessage());
    this.value = new TString(e.getMessage());
    this.token = e.getToken();
  }

  public Token getToken() {
    return token;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "Error: " + value;
  }
}
