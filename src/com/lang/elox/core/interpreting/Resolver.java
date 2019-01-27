package com.lang.elox.core.interpreting;

import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.interfaces.ExprVisitor;
import com.lang.elox.representation.interfaces.StmtVisitor;
import com.lang.elox.representation.parsetime.Expr;
import com.lang.elox.representation.parsetime.Stmt;
import com.lang.elox.utils.ErrorReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public final class Resolver implements ExprVisitor<Void>, StmtVisitor<Void> {
  private final Interpreter interpreter;
  private final ErrorReporter reporter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  private ClassType currentClass = ClassType.NONE;

  private enum FunctionType {
    NONE,
    FUNCTION,
    METHOD,
    INITIALIZER,
    LAMBDA
  }

  private enum ClassType {
    NONE,
    CLASS,
    SUBCLASS
  }

  public Resolver(Interpreter interpreter, ErrorReporter reporter) {
    this.interpreter = interpreter;
    this.reporter = reporter;
  }

  public void resolve(List<Stmt> statements) {
    statements.forEach(this::resolve);
  }

  private void resolve(Stmt stmt) {
    stmt.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) return;
    Map<String, Boolean> scope = scopes.peek();
    if (scope.containsKey(name.lexeme)) {
      reporter.error(name, "Variable already declared in this scope.");
    }
    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme, true);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - i - 1);
        return;
      }
    }
  }

  private void resolveLambda(Expr.Lambda lambda, FunctionType type) {
    FunctionType enclosingLambda = currentFunction;
    currentFunction = type;
    beginScope();
    for (Token param : lambda.params) {
      declare(param);
      define(param);
    }
    resolve(lambda.body);
    endScope();
    currentFunction = enclosingLambda;
  }

  private void resolveFunction(Stmt.Function function, FunctionType type) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;
    beginScope();
    for (Token param : function.params) {
      declare(param);
      define(param);
    }
    resolve(function.body);
    endScope();
    currentFunction = enclosingFunction;
  }

  @Override
  public Void visit(Expr.TypeLiteral expr) {
    return null;
  }

  @Override
  public Void visit(Stmt.Throw stmt) {
    return null;
  }

  @Override
  public Void visit(Stmt.Try stmt) {
    beginScope();
    resolve(stmt.tryStmt);
    endScope();

    beginScope();
    declare(stmt.parameter);
    define(stmt.parameter);
    resolve(stmt.catchStmt);
    endScope();
    return null;
  }

  @Override
  public Void visit(Stmt.Import stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  @Override
  public Void visit(Expr.DictLiteral expr) {
    for (Map.Entry<Expr, Expr> entry : expr.map.entrySet()) {
      resolve(entry.getKey());
      resolve(entry.getValue());
    }
    return null;
  }

  @Override
  public Void visit(Expr.IndexGet expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visit(Expr.IndexSet expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visit(Expr.ListLiteral expr) {
    return null;
  }

  @Override
  public Void visit(Expr.Super expr) {
    if (currentClass == ClassType.NONE) {
      reporter.error(expr.keyword, "Cannot use 'super' outside of a class.");
    } else if (currentClass != ClassType.SUBCLASS) {
      reporter.error(expr.keyword, "Cannot use 'super' in a class with no superclass.");
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visit(Expr.This expr) {
    if (currentClass == ClassType.NONE) {
      reporter.error(expr.keyword, "Cannot use 'this' outside of class.");
      return null;
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }

  @Override
  public Void visit(Expr.Set expr) {
    resolve(expr.value);
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visit(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visit(Stmt.Class stmt) {
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;

    declare(stmt.name);

    if (stmt.superclass != null) {
      currentClass = ClassType.SUBCLASS;
      resolve(stmt.superclass);
    }

    define(stmt.name);

    if (stmt.superclass != null) {
      beginScope();
      scopes.peek().put("super", true);
    }

    beginScope();
    scopes.peek().put("this", true);

    for (Stmt.Function method : stmt.methods) {
      FunctionType declaration = FunctionType.METHOD;
      if (method.name.lexeme.equals("__init__"))
        declaration = FunctionType.INITIALIZER;
      resolveFunction(method, declaration);
    }

    endScope();

    if (stmt.superclass != null) endScope();
    currentClass = enclosingClass;
    return null;
  }

  @Override
  public Void visit(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }

  @Override
  public Void visit(Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
    }
    define(stmt.name);
    return null;
  }

  @Override
  public Void visit(Expr.Variable expr) {
    if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
      reporter.error(expr.name, "Cannot refer to itself in initializer.");
    }

    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visit(Expr.Assign expr) {
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visit(Expr.Lambda expr) {
    resolveLambda(expr, FunctionType.LAMBDA);
    return null;
  }

  @Override
  public Void visit(Stmt.Function stmt) {
    declare(stmt.name);
    define(stmt.name);

    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  @Override
  public Void visit(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visit(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) resolve(stmt.elseBranch);
    return null;
  }

  @Override
  public Void visit(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE)
      reporter.error(stmt.keyword, "Cannot return from non-function scope.");
    if (stmt.value != null) {
      if (currentFunction == FunctionType.INITIALIZER)
        reporter.error(stmt.keyword, "Cannot return a value from an initialzier.");
      resolve(stmt.value);
    }
    return null;
  }

  @Override
  public Void visit(Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }

  @Override
  public Void visit(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visit(Expr.Instance expr) {
    resolve(expr.clazz);
    expr.arguments.forEach(this::resolve);
    return null;
  }

  @Override
  public Void visit(Expr.Call expr) {
    resolve(expr.callee);
    expr.arguments.forEach(this::resolve);
    return null;
  }

  @Override
  public Void visit(Expr.Grouping expr) {
    resolve(expr.expr);
    return null;
  }

  @Override
  public Void visit(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visit(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visit(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visit(Expr.StringLiteral expr) {
    return null;
  }
}
