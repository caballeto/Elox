package com.lang.elox.representation.runtime.instances;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.classes.TStringClass;

public final class TString extends TStringClass {
  private String string;

  public TString(char c) {
    this.string = "" + c;
  }

  public TString() {
    this.string = "";
  }

  public TString(String string) {
    this.string = string;
  }

  public TString(TString t) {
    this.string = t.string;
  }

  @Override
  public Object get(Token name) {
    ICallable method = TStringClass.findMethod(this, name);
    if (method != null) return method;
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  public String string() {
    return string;
  }

  @Override
  public String toString() {
    return string;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (this.getClass() != o.getClass()) return false;
    TString that = (TString) o;
    return this.string.equals(that.string);
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }
}
