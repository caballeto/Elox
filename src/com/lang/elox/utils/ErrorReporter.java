package com.lang.elox.utils;

import com.lang.elox.errors.RuntimeError;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.core.scanning.TokenType;

import java.util.List;

public final class ErrorReporter {
  private static final String SYNTAX_ERROR = "SyntaxError";
  private static final String PARSE_ERROR = "ParseError";
  private static final String RUNTIME_ERROR = "RuntimeError";

  public boolean hadError;
  public boolean hadRuntimeError;

  public void syntaxError(int line, String message) {
    reportWithPrefix(line, SYNTAX_ERROR, message);
  }

  public void parseError(int line, String message) {
    reportWithPrefix(line, PARSE_ERROR, message);
  }

  public void runtimeError(int line, String message) {
    reportWithPrefix(line, RUNTIME_ERROR, message);
  }

  public void error(int line, String message) {
    report(line, "", message);
  }

  public void error(String message) {
    System.err.println(message);
  }

  public void runtimeError(RuntimeError error) {
    System.err.println("[line " + error.token.line + "] " + error.getMessage());
    hadRuntimeError = true;
  }

  public void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end ", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'",  message);
    }
  }

  public void circularImportError(List<String> files) {
    for (String file : files) {
      System.err.print(file + " ");
    }
  }

  private void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  private void reportWithPrefix(int line, String prefix, String message) {
    System.err.println("[line " + line + "] " + prefix + ": " + message);
    hadError = true;
  }
}
