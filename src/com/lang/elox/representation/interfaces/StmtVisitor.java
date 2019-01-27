package com.lang.elox.representation.interfaces;

import com.lang.elox.representation.parsetime.Stmt;
import com.lang.elox.representation.parsetime.Stmt.*;

public interface StmtVisitor<T> {
  T visit(Expression stmt);
  T visit(Var stmt);
  T visit(Block stmt);
  T visit(If stmt);
  T visit(While stmt);
  T visit(Function stmt);
  T visit(Stmt.Return stmt);
  T visit(Stmt.Class stmt);
  T visit(Import stmt);
  T visit(Try stmt);
  T visit(Throw stmt);
}
