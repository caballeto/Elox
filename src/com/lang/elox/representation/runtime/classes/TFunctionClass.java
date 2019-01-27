package com.lang.elox.representation.runtime.classes;

import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;

public class TFunctionClass extends Type {
  @Override
  public Object get(Token name) {
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  @Override
  public String toString() {
    return "[Type: Function]";
  }
}
