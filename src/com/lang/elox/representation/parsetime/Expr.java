package com.lang.elox.representation.parsetime;

import com.lang.elox.representation.interfaces.ExprVisitor;
import com.lang.elox.core.scanning.Token;

import java.util.List;
import java.util.Map;

// Data hiding is not main here

public abstract class Expr {
  public abstract <T> T accept(ExprVisitor<T> visitor);

  public static class Unary extends Expr {
    public final Token operator;
    public final Expr right;

    public Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Binary extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Grouping extends Expr {
    public final Expr expr;

    public Grouping(Expr expr) {
      this.expr = expr;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Logical extends Expr {
    public final Expr left;
    public final Token operator;
    public final Expr right;

    public Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Literal extends Expr {
    public final Object value;

    public Literal(Object value) {
      this.value = value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class StringLiteral extends Expr {
    public final String value;

    public StringLiteral(String value) {
      this.value = value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Variable extends Expr {
    public final Token name;

    public Variable(Token name) {
      this.name = name;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Assign extends Expr {
    public final Token name;
    public final Expr value;

    public Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Call extends Expr {
    public final Expr callee;
    public final Token paren;
    public final List<Expr> arguments;

    public Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Instance extends Expr {
    public final Expr clazz;
    public final Token paren;
    public final List<Expr> arguments;

    public Instance(Expr clazz, Token paren, List<Expr> arguments) {
      this.clazz = clazz;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Get extends Expr {
    public final Expr object;
    public final Token name;

    public Get(Expr object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Set extends Expr {
    public final Expr object;
    public final Token name;
    public final Expr value;

    public Set(Expr object, Token name, Expr value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class This extends Expr {
    public final Token keyword;

    public This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class ListLiteral extends Expr {
    public final Token paren;
    public final List<Expr> elements;

    public ListLiteral(Token paren, List<Expr> elements) {
      this.paren = paren;
      this.elements = elements;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class IndexGet extends Expr {
    public final Expr object;
    public final Expr index;
    public final Token token;

    public IndexGet(Expr object, Expr index, Token token) {
      this.object = object;
      this.index = index;
      this.token = token;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class IndexSet extends Expr {
    public final Expr object;
    public final Expr index;
    public final Expr value;
    public final Token token;

    public IndexSet(Expr object, Expr index, Expr value, Token token) {
      this.object = object;
      this.index = index;
      this.value = value;
      this.token = token;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class DictLiteral extends Expr {
    public final Token paren;
    public final Map<Expr, Expr> map;

    public DictLiteral(Token paren, Map<Expr, Expr> map) {
      this.paren = paren;
      this.map = map;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class TypeLiteral extends Expr {
    public final Token name;

    public TypeLiteral(Token name) {
      this.name = name;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Lambda extends Expr {
    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;

    public Lambda(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Super extends Expr {
    public final Token keyword;
    public final Token method;

    public Super(Token keyword, Token method) {
      this.keyword = keyword;
      this.method = method;
    }

    @Override
    public <T> T accept(ExprVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }
}
