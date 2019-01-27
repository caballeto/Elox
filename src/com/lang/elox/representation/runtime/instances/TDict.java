package com.lang.elox.representation.runtime.instances;

import com.lang.elox.representation.interfaces.ICallable;
import com.lang.elox.representation.interfaces.IIndexable;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.runtime.classes.TDictClass;

import java.util.Iterator;
import java.util.Map;

public final class TDict extends TDictClass implements IIndexable<Object> {
  private Map<Object, Object> map;

  public TDict(Map<Object, Object> map) {
    this.map = map;
  }

  public Map<Object, Object> getMap() {
    return map;
  }

  @Override
  public Object get(Token name) {
    ICallable method = TDictClass.findMethod(this, name);
    if (method != null) return method;
    throw new RuntimeError(name, "Undefined method '" + name.lexeme + "'.");
  }

  public int size() {
    return map.size();
  }

  @Override
  public Object index(Object argument) {
    return map.get(argument);
  }

  @Override
  public void set(Object index, Object value) {
    map.put(index, value);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null) return false;
    if (o.getClass() != this.getClass()) return false;
    TDict dict = (TDict) o;
    return this.map.equals(dict.map);
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public String toString() {
    Iterator var1 = this.map.entrySet().iterator();
    if (!var1.hasNext()) {
      return "{}";
    } else {
      StringBuilder var2 = new StringBuilder();
      var2.append("{ ");

      while(true) {
        Map.Entry var3 = (Map.Entry)var1.next();
        Object var4 = var3.getKey();
        Object var5 = var3.getValue();
        boolean var6 = (var4 instanceof TString), var7 = (var5 instanceof TString);
        if (var6) var2.append('"');
        var2.append(var4 == this ? "(this Map)" : var4);
        if (var6) var2.append('"');
        var2.append(" : ");
        if (var7) var2.append('"');
        var2.append(var5 == this ? "(this Map)" : var5);
        if (var7) var2.append('"');
        if (!var1.hasNext()) {
          return var2.append(" }").toString();
        }

        var2.append(',').append(' ');
      }
    }
  }

  public boolean contains(Object index) {
    return map.containsKey(index);
  }
}
