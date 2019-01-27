package com.lang.elox.representation.runtime.instances;

import com.lang.elox.core.Environment;
import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.errors.Return;
import com.lang.elox.representation.parsetime.Stmt;
import com.lang.elox.representation.runtime.classes.TFunctionClass;

import java.util.List;

public final class TFunction extends TFunctionClass implements ICallable {
  private final Stmt.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;

  public TFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.declaration = declaration;
    this.closure = closure;
    this.isInitializer = isInitializer;
  }

  public TFunction bind(TInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new TFunction(declaration, environment, isInitializer);
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      return returnValue.value;
    }

    if (isInitializer) return closure.getAt(0, "this");
    return null;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public String toString() {
    return "[Function: " + declaration.name.lexeme + "]";
  }
}
