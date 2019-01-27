package com.lang.elox.representation.runtime.instances;

import com.lang.elox.core.Environment;

public final class TModule  {
  private String name;
  private Environment environment;

  public TModule(String name, Environment environment) {
    this.name = name;
    this.environment = environment;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public String getName() {
    return name;
  }
}
