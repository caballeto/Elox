package com.lang.elox.representation.interfaces;

import static com.lang.elox.representation.parsetime.Expr.*;

public interface ExprVisitor<T> {
  T visit(Unary expr);
  T visit(Binary expr);
  T visit(Grouping expr);
  T visit(Literal expr);
  T visit(Logical expr);
  T visit(StringLiteral expr);
  T visit(Variable expr);
  T visit(Assign expr);
  T visit(Call expr);
  T visit(Instance expr);
  T visit(Get expr);
  T visit(Set expr);
  T visit(Super expr);
  T visit(This expr);
  T visit(ListLiteral expr);
  T visit(DictLiteral expr);
  T visit(IndexSet expr);
  T visit(IndexGet expr);
  T visit(Lambda expr);
  T visit(TypeLiteral expr);
}
