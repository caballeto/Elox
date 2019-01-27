package com.lang.elox.representation.runtime.instances;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.representation.interfaces.IIndexable;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.classes.TListClass;

import java.util.List;
import java.util.Objects;

public final class TList extends TListClass implements IIndexable<Integer> {
  private List<Object> elements;

  public TList(List<Object> elements) {
    this.elements = elements;
  }

  public int size() {
    return elements.size();
  }

  public void assign(TList list) {
    this.elements.addAll(list.getElements());
  }

  public List<Object> getElements() {
    return elements;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (o.getClass() != this.getClass()) return false;
    TList that = (TList) o;
    return equals(this.elements, that.elements);
  }

  @Override
  public int hashCode() {
    return Objects.hash(elements);
  }

  private boolean equals(List<Object> o1, List<Object> o2) {
    if (o1.size() != o2.size()) return false;
    for (int i = 0; i < o1.size(); i++) {
      if (!o1.get(i).equals(o2.get(i))) return false;
    }

    return true;
  }

  @Override
  public Object index(Integer arg) {
    return elements.get(arg);
  }

  @Override
  public void set(Integer index, Object value) {
    elements.set(index, value);
  }

  @Override
  public Object get(Token name) {
    ICallable method = TListClass.findMethod(this, name);
    if (method != null) return method;
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  @Override
  public String toString() {
    StringBuilder text = new StringBuilder("[");
    for (int i = 0; i < elements.size(); i++) {
      text.append(elements.get(i).toString());
      if (i != elements.size() - 1) text.append(", ");
    }
    text.append(']');
    return text.toString();
  }
}
