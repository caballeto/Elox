package com.lang.elox.representation.runtime.classes;

import com.lang.elox.core.scanning.Token;

public abstract class Type  {
  public abstract Object get(Token name);
}
