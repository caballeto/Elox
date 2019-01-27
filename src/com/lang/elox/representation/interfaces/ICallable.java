package com.lang.elox.representation.interfaces;

import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.core.scanning.Token;

import java.util.List;

public interface ICallable {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);

  static <T> ICallable build(INativeCallable<T> method, T object, Token name) {
    return new ICallable() {
      @Override
      public int arity() {
        return method.arity();
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return method.call(interpreter, object, name, arguments);
      }

      @Override
      public String toString() {
        return "[Function: " + name.lexeme + "]";
      }
    };
  }
}
