package com.lang.elox.representation.interfaces;

import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.core.scanning.Token;

import java.util.List;

public interface INativeCallable<T> {
  int arity();
  Object call(Interpreter interpreter, T object, Token token, List<Object> arguments);
}
