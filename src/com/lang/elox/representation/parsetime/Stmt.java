package com.lang.elox.representation.parsetime;

import com.lang.elox.representation.interfaces.StmtVisitor;
import com.lang.elox.core.scanning.Token;

import java.util.List;

public abstract class Stmt {
  public abstract <T> T accept(StmtVisitor<T> visitor);

  public static class Expression extends Stmt {
    public final Expr expression;

    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Var extends Stmt {
    public final Token name;
    public final Expr initializer;

    public Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Block extends Stmt {
    public final List<Stmt> statements;

    public Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class If extends Stmt {
    public final Expr condition;
    public final Stmt thenBranch;
    public final Stmt elseBranch;

    public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class While extends Stmt {
    public final Expr condition;
    public final Stmt body;

    public While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Function extends Stmt {
    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;

    public Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Return extends Stmt {
    public final Token keyword;
    public final Expr value;

    public Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Class extends Stmt {
    public final Token name;
    public Expr.Variable superclass;
    public final List<Stmt.Function> methods;

    public Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Import extends Stmt {
    public final Token name;
    public final List<Stmt> statements;

    public Import(Token name, List<Stmt> statements) {
      this.name = name;
      this.statements = statements;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Try extends Stmt {
    public final Token parameter;
    public final List<Stmt> tryStmt;
    public final List<Stmt> catchStmt;

    public Try(Token parameter, List<Stmt> tryStmt, List<Stmt> catchStmt) {
      this.parameter = parameter;
      this.tryStmt = tryStmt;
      this.catchStmt = catchStmt;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }

  public static class Throw extends Stmt {
    public final Token token;
    public final Expr expr;

    public Throw(Token token, Expr expr) {
      this.token = token;
      this.expr = expr;
    }

    @Override
    public <T> T accept(StmtVisitor<T> visitor) {
      return visitor.visit(this);
    }
  }
}
