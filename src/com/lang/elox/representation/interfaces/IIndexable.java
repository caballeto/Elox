package com.lang.elox.representation.interfaces;

public interface IIndexable<T> {
  Object index(T argument);
  void set(T index, Object value);
}