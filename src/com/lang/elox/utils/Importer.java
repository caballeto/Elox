package com.lang.elox.utils;

import com.lang.elox.core.Environment;
import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.representation.parsetime.Stmt;
import com.lang.elox.representation.runtime.instances.TModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public final class Importer {
  private final Interpreter interpreter;
  private final ErrorReporter reporter;

  public Importer(Interpreter interpreter, ErrorReporter reporter, String filename) {
    this.interpreter = interpreter;
    this.reporter = reporter;
  }

  public TModule buildModule(String filename, List<Stmt> statements) {
    try {
      Environment environment = new Environment();
      interpreter.executeBlock(statements, environment);
      return new TModule(filename, environment);
    } catch (Exception e) {
      reporter.error(e.getMessage());
    }

    return null;
  }

  public void importAll(TModule module, Environment environment) {
    importToEnv(module.getEnvironment(), environment);
  }

  public void importModule(TModule module, Environment environment) {
    environment.define(module.getName(), module);
  }

  public String readAll(String filename) throws IOException {
    return new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
  }

  private void importToEnv(Environment from, Environment to) {
    for (Map.Entry<String, Object> entry : from.getValues().entrySet()) {
      to.define(entry.getKey(), entry.getValue());
    }
  }
}
