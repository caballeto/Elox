package com.lang.elox.core.interpreting;

import com.lang.elox.core.Environment;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.errors.Return;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.representation.interfaces.ExprVisitor;
import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.representation.interfaces.IIndexable;
import com.lang.elox.representation.interfaces.StmtVisitor;
import com.lang.elox.representation.parsetime.Expr;
import com.lang.elox.representation.parsetime.Stmt;
import com.lang.elox.representation.runtime.classes.*;
import com.lang.elox.representation.runtime.instances.*;
import com.lang.elox.utils.ErrorReporter;
import com.lang.elox.utils.Importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
  private final ErrorReporter reporter;
  private Importer importer;
  private Environment globals = new Environment();
  private Environment environment = globals;
  private Map<Expr, Integer> locals = new HashMap<>();

  public Interpreter(ErrorReporter reporter) {
    this.reporter = reporter;

    globals.define("write", new ICallable() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.print(arguments.get(0));
        return null;
      }

      @Override
      public String toString() {
        return "[Function: write]";
      }
    });

    globals.define("clock", new ICallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() {
        return "[Function: clock]";
      }
    });

    globals.define("writeln", new ICallable() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.println(arguments.get(0));
        return null;
      }

      @Override
      public String toString() {
        return "[Function: writeln]";
      }
    });
  }

  public Map<Expr, Integer> getLocals() {
    return locals;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void interpret(List<Stmt> statements, String filename) {
    try {
      importer = new Importer(this, reporter, filename);// FIXME: 1/26/19 is it a right place for importer?
      statements.forEach(this::execute);
    } catch (RuntimeError error) {
      reporter.runtimeError(error);
    } catch (TException error) {
      reporter.runtimeError(new RuntimeError(error.getToken(), "Error: " + error.getValue()));
    } catch (StackOverflowError e) {
      reporter.error("Error: stack overflow.");
    } catch (Exception e) {
      reporter.error("Error: " + e.getMessage());
    }
  }

  public void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  // overrides

  @Override
  public Object visit(Expr.Super expr) {
    int distance = locals.get(expr);
    TClass superclass = (TClass) environment.getAt(distance, "super");
    TInstance object = (TInstance) environment.getAt(distance - 1, "this");
    TFunction method = superclass.findMethod(object, expr.method.lexeme);
    if (method == null) {
      throw new RuntimeError(expr.method, "Undefined method '" + expr.method.lexeme + "'.");
    }

    return method;
  }

  @Override
  public Object visit(Expr.TypeLiteral expr) {
    switch (expr.name.lexeme) {
      case "Object": return new TObjectClass();
      case "Int": return new TIntClass();
      case "Double": return new TDoubleClass();
      case "Boolean": return new TBooleanClass();
      case "String": return new TStringClass();
      case "List": return new TListClass();
      case "Dict": return new TDictClass();
      case "Function": return new TFunctionClass();
      case "Lambda": return new TLambdaClass();
      default:
        throw new RuntimeError(expr.name, "Undefined type '" + expr.name.lexeme + "'.");
    }
  }

  @Override
  public Void visit(Stmt.Throw stmt) {
    throw new TException(stmt.token, evaluate(stmt.expr));
  }

  @Override
  public Void visit(Stmt.Import stmt) {
    TModule module = importer.buildModule((String) stmt.name.literal, stmt.statements);
    if (module == null) throw new RuntimeError(stmt.name, "Error while importing file.");
    importer.importAll(module, environment);
    return null;
  }

  @Override
  public Object visit(Expr.IndexSet expr) {
    Object object = evaluate(expr.object);
    Object index = evaluate(expr.index);
    Object value = evaluate(expr.value);

    if (object == null)
      throw new RuntimeError(expr.token, "Null pointer exception.");
    if (!(object instanceof IIndexable))
      throw new RuntimeError(expr.token, "Object is not indexable.");

    if (object instanceof TList) {
      TList var1 = (TList) object;
      validateListKey(expr.token, var1, index);
      var1.set((int) index, value);
    } else if (object instanceof TDict) {
      validateDictKey(expr.token, index);
      TDict var1 = (TDict) object;
      var1.set(index, value);
    }

    return value;
  }

  @Override
  public Object visit(Expr.IndexGet expr) {
    Object object = evaluate(expr.object);
    Object index = evaluate(expr.index);

    if (!(object instanceof IIndexable))
      throw new RuntimeError(expr.token, "Only lists/dicts are indexable.");

    if (object instanceof TList) {
      TList var1 = (TList) object;
      validateListKey(expr.token, var1, index);
      return var1.index((int) index);
    } else if (object instanceof TDict) {
      validateDictKey(expr.token, index);
      TDict var1  = (TDict) object;
      if (!var1.contains(index))
        throw new RuntimeError(expr.token, "No such key '" + index + "'.");
      return var1.index(index);
    } else {
      throw new RuntimeError(expr.token, "Only lists/dicts are indexable.");
    }
  }

  @Override
  public Object visit(Expr.DictLiteral expr) {
    Map<Object, Object> map = new HashMap<>();
    for (Map.Entry<Expr, Expr> entry : expr.map.entrySet()) {
      Object key = evaluate(entry.getKey());
      Object value = evaluate(entry.getValue());

      if (!(key instanceof TString) && !(key instanceof Integer) && !(key instanceof Double)) {
        throw new RuntimeError(expr.paren, "Dictionary supports only string/integer/double keys.");
      }

      map.put(key, value);
    }

    return new TDict(map);
  }

  @Override
  public Object visit(Expr.ListLiteral expr) {
    List<Object> elements = new ArrayList<>();
    for (Expr expression : expr.elements) {
      elements.add(evaluate(expression));
    }

    return new TList(elements);
  }

  @Override
  public Object visit(Expr.This expr) {
    return lookUpVariable(expr.keyword, expr);
  }

  @Override
  public Object visit(Expr.Set expr) {
    Object object = evaluate(expr.object);

    if (!(object instanceof TInstance)) {
      throw new RuntimeError(expr.name, "Only instances have fields.");
    }

    Object value = evaluate(expr.value);
    ((TInstance) object).set(expr.name, value);
    return value;
  }

  @Override
  public Void visit(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null) value = evaluate(stmt.value);
    throw new Return(value);
  }

  @Override
  public Object visit(Expr.Lambda expr) {
    return new TLambda(expr, environment);
  }

  @Override
  public Void visit(Stmt.Try stmt) {
    try {
      executeBlock(stmt.tryStmt, new Environment(environment));
    } catch (RuntimeError e) {
      Environment environment = new Environment(this.environment);
      environment.define(stmt.parameter.lexeme, new TException(e));
      executeBlock(stmt.catchStmt, environment);
    } catch (TException e) {
      Environment environment = new Environment(this.environment);
      environment.define(stmt.parameter.lexeme, e.getValue());
      executeBlock(stmt.catchStmt, environment);
    }

    return null;
  }

  @Override
  public Void visit(Stmt.Function stmt) {
    TFunction function = new TFunction(stmt, environment, false);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visit(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.body);
    }

    return null;
  }

  @Override
  public Void visit(Stmt.If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }

    return null;
  }

  @Override
  public Void visit(Stmt.Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visit(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Void visit(Stmt.Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Object visit(Expr.Assign expr) {
    Object value = evaluate(expr.value);

    Integer distance = locals.get(expr);
    if (distance != null) {
      environment.assignAt(distance, expr.name, value);
    } else {
      globals.assign(expr.name, value);
    }

    return value;
  }


  @Override
  public Object visit(Expr.Get expr) {
    Object object = evaluate(expr.object);
    if (object == null)
      throw new RuntimeError(expr.name, "Null pointer exception.");

    if (object instanceof TInstance) {
      return ((TInstance) object).get(expr.name);
    }

    if (object instanceof TString) {
      return ((TString) object).get(expr.name);
    }

    if (object instanceof TList) {
      return ((TList) object).get(expr.name);
    }

    if (object instanceof TDict) {
      return ((TDict) object).get(expr.name);
    }

    if (object instanceof Type) {
      return ((Type) object).get(expr.name);
    }

    throw new RuntimeError(expr.name,"Error: '" + object + "' can't have properties.");
  }

  @Override
  public Void visit(Stmt.Class stmt) {
    Object superclass = null;
    if (stmt.superclass != null) {
      superclass = evaluate(stmt.superclass);
      if (!(superclass instanceof TClass)) {
        throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
      }
    }

    environment.define(stmt.name.lexeme, null);

    if (stmt.superclass != null) {
      environment = new Environment(environment);
      environment.define("super", superclass);
    }

    Map<String, TFunction> methods = new HashMap<>();
    for (Stmt.Function method : stmt.methods) {
      TFunction function = new TFunction(method, environment, method.name.lexeme.equals("this"));
      methods.put(method.name.lexeme, function);
    }

    TClass clazz = new TClass(stmt.name.lexeme, (TClass) superclass, methods);
    if (superclass != null) environment = environment.getEnclosing();
    environment.assign(stmt.name, clazz);
    return null;
  }

  @Override
  public Object visit(Expr.Instance expr) {
    Object callee = evaluate(expr.clazz);
    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof TClass)) {
      throw new RuntimeError(expr.paren, "Can't construct object from non-class.");
    }

    ICallable clazz = (ICallable) callee;
    if (arguments.size() != clazz.arity()) {
      throw new RuntimeError(expr.paren, "Expected " +
          clazz.arity() + " arguments to constructor but got " +
          arguments.size() + ".");
    }

    return clazz.call(this, arguments);
  }

  @Override
  public Object visit(Expr.Call expr) {
    Object callee = evaluate(expr.callee);
    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof ICallable))
      throw new RuntimeError(expr.paren, "Can call only functions.");
    if (callee instanceof TClass)
      throw new RuntimeError(expr.paren, "Can't instantiate without 'new'.");

    ICallable function = (ICallable) callee;
    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " +
          function.arity() + " arguments but got " +
          arguments.size() + ".");
    }
    return function.call(this, arguments);
  }

  @Override
  public Object visit(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case NOT:
        return !isTruthy(right);
      case MINUS: {
        if (checkInts(right))
          return - (int) right;
        else if (checkIntDoubles(right))
          return - (double) right;
        else error(expr.operator, "Operands must be integer.");
      }
      case BIT_COMPL: {
        if (checkInts(right))
          return ~ (int) right;
        else error(expr.operator, "Operand must be integer.");
      }
    }

    return null;
  }

  @Override
  public Object visit(Expr.Grouping expr) {
    return evaluate(expr.expr);
  }

  @Override
  public Object visit(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case BIT_OR: {
        if (checkInts(left, right))
          return (int) left | (int) right;
        else error(expr.operator, "Invalid operands for '|' : '" + left + "' and '" + right + "'.");
      }
      case BIT_XOR: {
        if (checkInts(left, right))
          return (int) left ^ (int) right;
        else error(expr.operator, "Invalid operands for '^' : '" + left + "' and '" + right + "'.");
      }
      case BIT_LEFT: {
        if (checkInts(left, right))
          return (int) left << (int) right;
        else error(expr.operator, "Invalid operands for '<<' : '" + left + "' and '" + right + "'.");
      }
      case BIT_RIGHT: {
        if (checkInts(left, right))
          return (int) left >> (int) right;
        else error(expr.operator, "Invalid operands for '>>' : '" + left + "' and '" + right + "'.");
      }
      case BIT_AND: {
        if (checkInts(left, right))
          return (int) left & (int) right;
        else error(expr.operator, "Invalid operands to '&' : '" + left + "' and '" + right + "'.");
      }
      case NOT_EQUAL:
        return !isEqual(left, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);
      case IS:
        if (left == null || !isType(right))
          throw new RuntimeError(expr.operator, "Invalid arguments to 'is'.");
        if (left instanceof TClass)
          throw new RuntimeError(expr.operator, "Invalid argument to IS: 'Class'.");
        if (left instanceof  Boolean && right instanceof TBooleanClass) return true;
        if (left instanceof Integer && right instanceof TIntClass) return true;
        if (left instanceof Double && right instanceof TDoubleClass) return true;
        if (left instanceof TInstance && right instanceof TClass)
          return isInstance(left, right);
        return right.getClass().isInstance(left);

      // #TODO fix garbage here
      case GREATER:{
        if (left instanceof TString && right instanceof TString)
          return ((TString) left).string().compareTo(((TString) right).string()) > 0;
        if (checkInts(left, right)) return (int) left > (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left > (double) right;
        } else error(expr.operator, "Invalid operands for '>' : '" + left + "' and '" + right + "'.");
      }
      case GREATER_EQUAL: {
        if (left instanceof TString && right instanceof TString)
          return ((TString) left).string().compareTo(((TString) right).string()) >= 0;
        if (checkInts(left, right)) return (int) left >= (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left >= (double) right;
        } else error(expr.operator, "Invalid operands for '>=' : '" + left + "' and '" + right + "'.");
      }
      case LESS: {
        if (left instanceof TString && right instanceof TString)
          return ((TString) left).string().compareTo(((TString) right).string()) < 0;
        if (checkInts(left, right)) return (int) left < (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left < (double) right;
        } else error(expr.operator, "Invalid operands for '<' : '" + left + "' and '" + right + "'.");
      }
      case LESS_EQUAL: {
        if (left instanceof TString && right instanceof TString)
          return ((TString) left).string().compareTo(((TString) right).string()) <= 0;
        if (checkInts(left, right)) return (int) left <= (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left <= (double) right;
        } else error(expr.operator, "Invalid operands for '<=' : '" + left + "' and '" + right + "'.");
      }
      case PLUS: {
        if (left instanceof TList && right instanceof TList) {
          ((TList) left).assign((TList) right);
          return left;
        }
        if (left instanceof TString && right instanceof TString)
          return new TString(((TString) left).string() + ((TString) right).string());
        if (left instanceof TString || right instanceof TString) {
          return new TString(left.toString() + right.toString());
        }

        if (checkInts(left, right))
          return (int) left + (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left + (double) right;
        } else error(expr.operator, "Invalid operands for '+' : '" + left + "' and '" + right + "'.");
      }
      case MINUS: {
        if (checkInts(left, right))
          return (int) left - (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left - (double) right;
        } else error(expr.operator, "Invalid operands for '-' : '" + left + "' and '" + right + "'.");
      }
      case STAR: {
        if (checkInts(left, right))
          return (int) left * (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left * (double) right;
        } else error(expr.operator, "Invalid operands for '*' : '" + left + "' and '" + right + "'.");
      }
      case SLASH: {
        if (checkInts(left, right))
          return (int) left / (int) right;
        else if (checkIntDoubles(left, right)) {
          if (left instanceof Integer) left = ((Integer) left).doubleValue();
          if (right instanceof Integer) right = ((Integer) right).doubleValue();
          return (double) left / (double) right;
        } else error(expr.operator, "Invalid operands for '/' : '" + left + "' and '" + right + "'.");
      }
      case REMAINDER: {
        if (checkInts(left, right))
          return (int) left % (int) right;
        else error(expr.operator, "Invalid operands for '%' : '" + left + "' and '" + right + "'.");
      }
    }

    return null;
  }

  @Override
  public Object visit(Expr.Literal expr) {
    return expr.value;
  }

  @Override
  public Object visit(Expr.Logical expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case OR:
        return isTruthy(left) ? left : right;
      case AND:
        return !isTruthy(left) ? left : right;
    }

    return null;
  }

  @Override
  public Object visit(Expr.Variable expr) {
    return lookUpVariable(expr.name, expr);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }

  @Override
  public Object visit(Expr.StringLiteral expr) {
    return new TString(expr.value);
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  public void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;
      statements.forEach(this::execute);
    } finally {
      this.environment = previous;
    }
  }

  private boolean isTruthy(Object a) {
    if (a == null) return false;
    if (a instanceof Boolean) return (boolean) a;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

  private void validateDictKey(Token token, Object key) {
    if (!(key instanceof TString) && !(key instanceof Integer) && !(key instanceof Double)) {
      throw new RuntimeError(token, "Dictionary supports only string/integer/double keys.");
    }
  }

  private void validateListKey(Token token, TList object, Object key) {
    if (!(key instanceof Integer))
      throw new RuntimeError(token, "Integer index expected.");
    int var2 = (int) key, len = object.size();
    if (var2 < 0 || var2 >= len)
      throw new RuntimeError(token, "Index is out of range.");
  }

  private void error(Token operator, String message) {
    throw new RuntimeError(operator, message);
  }

  private boolean isDoubleInteger(Object o) {
    return o instanceof Double || o instanceof Integer;
  }

  private boolean checkIntDoubles(Object... objects) {
    for (Object o : objects) {
      if (!isDoubleInteger(o)) return false;
    }

    return true;
  }

  private boolean checkInts(Object... objects) {
    for (Object o : objects) {
      if (!(o instanceof Integer)) return false;
    }

    return true;
  }

  private boolean isType(Object object) {
    if (object instanceof TIntClass ||
        object instanceof TDoubleClass ||
        object instanceof TBooleanClass ||
        object instanceof TStringClass ||
        object instanceof TObjectClass ||
        object instanceof TListClass ||
        object instanceof TLambdaClass ||
        object instanceof TFunctionClass ||
        object instanceof TClass ||
        object instanceof TDictClass) return true;
    return false;
  }

  private boolean isInstance(Object left, Object right) {
    if (!(right instanceof TClass)) return false;
    if (!(left instanceof TInstance)) return false;

    TClass type = (TClass) right;
    TClass instanceType = ((TInstance) left).getClazz();

    if (instanceType.equals(type)) return true;
    while (instanceType.getSuperclass() != null) {
      if (instanceType.getSuperclass().equals(type)) {
        return true;
      }

      instanceType = instanceType.getSuperclass();
    }

    return false;
  }
}
