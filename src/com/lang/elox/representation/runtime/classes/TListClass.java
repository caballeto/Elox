package com.lang.elox.representation.runtime.classes;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.representation.interfaces.INativeCallable;
import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.instances.TFunction;
import com.lang.elox.representation.runtime.instances.TLambda;
import com.lang.elox.representation.runtime.instances.TList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TListClass extends Type {
  private static Map<String, INativeCallable<TList>> methods = new HashMap<>();
  private static Map<String, INativeCallable<TList>> statics = new HashMap<>();

  static {
    methods.put("length", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        return object.size();
      }
    });

    methods.put("isEmpty", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        return object.getElements().isEmpty();
      }
    });

    methods.put("clear", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        object.getElements().clear();
        return null;
      }
    });

    methods.put("forEach", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        validateCallable(arguments.get(0), token);

        ICallable function = (ICallable) arguments.get(0);
        validateArity(function, 1, token);
        for (Object element : object.getElements()) {
          function.call(interpreter, Arrays.asList(element));
        }

        return null;
      }
    });

    methods.put("map", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        validateCallable(arguments.get(0), token);

        ICallable function = (ICallable) arguments.get(0);
        validateArity(function, 1, token);
        List<Object> elements = new ArrayList<>();
        for (Object element : object.getElements()) {
          elements.add(function.call(interpreter, Arrays.asList(element)));
        }

        return new TList(elements);
      }
    });

    methods.put("add", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        if (arguments.get(0) == null) throw new RuntimeError(token, "Null keys are forbidden.");
        object.getElements().add(arguments.get(0));
        return null;
      }
    });

    methods.put("pop", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        if (object.getElements().isEmpty()) throw new RuntimeError(token, "Array is empty.");
        return object.getElements().remove(object.size() - 1);
      }
    });

    methods.put("insert", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 2;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        if (arguments.get(1) == null) throw new RuntimeError(token, "Null keys are forbidden.");
        if (!(arguments.get(0) instanceof Integer))
          throw new RuntimeError(token, "Integer index expected.");
        Integer index = (Integer) arguments.get(0);
        if (index < 0 || index >= object.size())
          throw new RuntimeError(token, "Index is out of bounds.");
        object.getElements().add(index, arguments.get(1));
        return null;
      }
    });

    methods.put("remove", new INativeCallable<TList>() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, TList object, Token token, List<Object> arguments) {
        if (!(arguments.get(0) instanceof Integer))
          throw new RuntimeError(token, "Integer index expected.");
        int index = (Integer) arguments.get(0);
        if (index < 0 || index >= object.size())
          throw new RuntimeError(token, "Index is out bounds.");
        return object.getElements().remove(index);
      }
    });
  }

  public static ICallable findMethod(TList list, Token name) {
    INativeCallable<TList> method = methods.get(name.lexeme);
    if (method == null) return null;
    return ICallable.build(method, list, name);
  }

  @Override
  public Object get(Token name) {
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  private static void validateCallable(Object object, Token token) {
    if (!(object instanceof TLambda) &&
        !(object instanceof TFunction) &&
        !(object instanceof ICallable))
      throw new RuntimeError(token, "Error: argument is not callable.");
  }

  private static void validateArity(ICallable function, int arity, Token token) {
    if (function.arity() != arity) {
      throw new RuntimeError(token, "Function to 'forEach' expected to receive 1 argument.");
    }
  }

  @Override
  public String toString() {
    return "[Type: List]";
  }
}


