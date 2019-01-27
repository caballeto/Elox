package com.lang.elox.representation.runtime.instances;

import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.classes.TObjectClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TInstance extends TObjectClass {
  private TClass clazz;
  private final Map<String, Object> fields = new HashMap<>();

  public TInstance(TClass clazz) {
    this.clazz = clazz;
  }

  @Override
  public Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    TFunction method = clazz.findMethod(this, name.lexeme);
    if (method != null) return method;

    throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(fields);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (o.getClass() != this.getClass()) return false;
    TInstance instance = (TInstance) o;
    return this.fields.equals(instance.fields) && this.clazz.equals(instance.clazz);
  }

  public void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  public TClass getClazz() {
    return clazz;
  }

  @Override
  public String toString() {
    return "[Object: " + clazz.getName() + "]";
  }
}
