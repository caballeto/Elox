package com.lang.elox.representation.runtime.classes;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.representation.interfaces.INativeCallable;
import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.instances.TDict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TDictClass extends Type {
  private static Map<String, INativeCallable<TDict>> methods = new HashMap<>();
  private static Map<String, INativeCallable<TDict>> statics = new HashMap<>();

  static {
    // Dict instance methods
    methods.put("size", new INativeCallable<TDict>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TDict object, Token token, List<Object> arguments) {
        return object.size();
      }
    });

    methods.put("isEmpty", new INativeCallable<TDict>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TDict object, Token token, List<Object> arguments) {
        return object.getMap().isEmpty();
      }
    });

    methods.put("clear", new INativeCallable<TDict>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TDict object, Token token, List<Object> arguments) {
        object.getMap().clear();
        return null;
      }
    });

    // Dict type static methods
    // statics.put(...);
  }

  @Override
  public Object get(Token name) {
    Object method = statics.get(name.lexeme);
    if (method != null) return method;
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  public static ICallable findMethod(TDict dict, Token name) {
    INativeCallable<TDict> method = methods.get(name.lexeme);
    if (method == null) return null;
    return ICallable.build(method, dict, name);
  }

  @Override
  public String toString() {
    return "[Type: Dict]";
  }
}
