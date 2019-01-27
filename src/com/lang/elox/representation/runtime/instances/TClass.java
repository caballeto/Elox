package com.lang.elox.representation.runtime.instances;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.core.interpreting.Interpreter;

import java.util.List;
import java.util.Map;

public final class TClass implements ICallable {
  private final String name;
  private final TClass superclass;
  private final Map<String, TFunction> methods;

  public TClass(String name, TClass superclass, Map<String, TFunction> methods) {
    this.name = name;
    this.superclass = superclass;
    this.methods = methods;
  }

  public TFunction findMethod(TInstance instance, String name) {
    if (methods.containsKey(name)) {
      return methods.get(name).bind(instance);
    }

    if (superclass != null) {
      return superclass.findMethod(instance, name);
    }

    return null;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    TInstance instance = new TInstance(this);
    TFunction initializer = methods.get("__init__");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    return instance;
  }

  @Override
  public int arity() {
    TFunction initializer = methods.get("__init__");
    if (initializer == null) return 0;
    return initializer.arity();
  }

  @Override
  public String toString() {
    return "[Class: " + name + "]";
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (o.getClass() != this.getClass()) return false;
    TClass that = (TClass) o;
    return that.name.equals(this.name);
  }

  public TClass getSuperclass() {
    return superclass;
  }

  public String getName() {
    return name;
  }
}
