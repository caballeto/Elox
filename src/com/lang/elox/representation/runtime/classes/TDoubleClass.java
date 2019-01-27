package com.lang.elox.representation.runtime.classes;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.representation.interfaces.INativeCallable;
import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.instances.TString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TDoubleClass extends Type {
  private static Map<String, INativeCallable<TString>> statics = new HashMap<>();

  static {
    statics.put("parseDouble", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        if (!(arguments.get(0) instanceof TString))
          throw new RuntimeError(token, "Expected string argument.");
        Double x;
        try {
          x = Double.parseDouble(((TString) arguments.get(0)).string());
        } catch (NumberFormatException e) {
          throw new RuntimeError(token, "Invalid number format.");
        }
        return x;
      }
    });
  }

  @Override
  public Object get(Token name) {
    INativeCallable<TString> method = statics.get(name.lexeme);
    if (method != null) return ICallable.build(method, null, name);
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  @Override
  public String toString() {
    return "[Type: Double]";
  }
}
