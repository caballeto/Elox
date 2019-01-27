package com.lang.elox.representation.runtime.instances;

import com.lang.elox.core.Environment;
import com.lang.elox.representation.parsetime.Expr;
import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.errors.Return;
import com.lang.elox.representation.runtime.classes.TLambdaClass;

import java.util.List;

public final class TLambda extends TLambdaClass implements ICallable {
  private final Expr.Lambda declaration;
  private final Environment closure;

  public TLambda(Expr.Lambda declaration, Environment closure) {
    this.declaration = declaration;
    this.closure = closure;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return exception) {
      return exception.value;
    }

    return null;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public String toString() {
    return"[Lambda]";
  }
}
