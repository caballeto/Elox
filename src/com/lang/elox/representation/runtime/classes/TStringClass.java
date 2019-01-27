package com.lang.elox.representation.runtime.classes;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.representation.interfaces.INativeCallable;
import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.instances.TList;
import com.lang.elox.representation.runtime.instances.TString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TStringClass extends Type {
  private static Map<String, INativeCallable<TString>> methods = new HashMap<>();
  private static Map<String, INativeCallable<TString>> statics = new HashMap<>();

  static {
    methods.put("length", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        return object.string().length();
      }
    });

    methods.put("substring", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        checkInts(token, arguments.get(0), arguments.get(1));
        int var1 = (int) arguments.get(0), var2 = (int) arguments.get(1), len = object.string().length();
        if (var1 < 0 || var2 < var1 || var2 > len) throw new RuntimeError(token, "Index is out of range.");
        String s = object.string().substring(var1, var2);
        return new TString(s);
      }
    });

    methods.put("replaceAll", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        if (!(arguments.get(0) instanceof TString) || !(arguments.get(1) instanceof TString))
          throw new RuntimeError(token, "Expected string arguments to 'replaceAll'.");
        String var1 = arguments.get(0).toString(), var2 = arguments.get(1).toString();
        return new TString(object.string().replaceAll(var1, var2));
      }
    });

    methods.put("replace", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        if (!(arguments.get(0) instanceof TString) || !(arguments.get(1) instanceof TString))
          throw new RuntimeError(token, "Expected string arguments to 'replace'.");
        String var1 = arguments.get(0).toString(), var2 = arguments.get(1).toString();
        return new TString(object.string().replaceFirst(var1, var2));
      }
    });

    methods.put("reverse", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        return new TString(new StringBuilder(object.string()).reverse().toString());
      }
    });

    methods.put("charAt", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        checkInts(token, arguments.get(0));
        int var1 = (int) arguments.get(0);
        if (var1 < 0 || var1 >= object.string().length())
          throw new RuntimeError(token, "Char index '" + var1 + "' is out of range.");
        return new TString(object.string().charAt(var1));
      }
    });

    methods.put("split", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        if (!(arguments.get(0) instanceof TString))
          throw new RuntimeError(token, "Expected string delimiter.");

        TString delimiter = (TString) arguments.get(0);
        return new TList(Arrays.asList(object.string().split(delimiter.string())));
      }
    });

    methods.put("isEmpty", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        return object.string().isEmpty();
      }
    });

    // String type static methods
    statics.put("join", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        if (!(arguments.get(0) instanceof TString) || !(arguments.get(1) instanceof TList))
          throw new RuntimeError(token, "Error: 'join' has signature (string, list[string]).");
        List<String> list = new ArrayList<>();
        for (Object o : ((TList) arguments.get(1)).getElements()) {
          if (!(o instanceof TString)) {
            throw new RuntimeError(token, "Error: 'join' has signature (string, list[string]).");
          }
          list.add(((TString) o).string());
        }

        return new TString(String.join(((TString) arguments.get(0)).string(), list));
      }
    });

    statics.put("toString", new INativeCallable<TString>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TString object, Token token, List<Object> arguments) {
        return new TString(arguments.get(0).toString());
      }
    });
  }

  public static ICallable findMethod(TString string, Token name) {
    INativeCallable<TString> method = methods.get(name.lexeme);
    if (method == null) return null;
    return ICallable.build(method, string, name);
  }

  private static void checkInts(Token token, Object... objects) {
    for (Object o : objects) {
      if (!(o instanceof Integer))
        throw new RuntimeError(token, "Integer indices expected.");
    }
  }
gi
  @Override
  public Object get(Token name) {
    INativeCallable<TString> method = statics.get(name.lexeme);
    if (method != null) return ICallable.build(method, null, name);
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  @Override
  public String toString() {
    return "[Type: String]";
  }
}
