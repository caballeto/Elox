package com.lang.elox.errors;

import java.util.List;

public final class CircularImportError extends RuntimeException {
  private List<String> cycle;

  public CircularImportError(List<String> cycle) {
    super();
    this.cycle = cycle;
  }

  public List<String> cycle() {
    return cycle;
  }
}
