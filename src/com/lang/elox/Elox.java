package com.lang.elox;

import com.lang.elox.core.interpreting.Interpreter;
import com.lang.elox.core.interpreting.Resolver;
import com.lang.elox.core.parsing.Parser;
import com.lang.elox.core.scanning.Scanner;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.representation.parsetime.Stmt;
import com.lang.elox.utils.ErrorReporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class Elox {
  private static final ErrorReporter reporter = new ErrorReporter();
  private static final Interpreter interpreter = new Interpreter(reporter);

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage : ./elox [script] ");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()), path);

    if (reporter.hadError) System.exit(65);
    if (reporter.hadRuntimeError) System.exit(70);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    while (true) {
      System.out.print("> ");
      run(reader.readLine(), "");
      reporter.hadError = false;
      Parser.getValidator().clear(); // clears import error stack trace
    }
  }

  private static void run(String source, String filename) {
    Parser.getValidator().setFile(filename);
    Scanner scanner = new Scanner(source, reporter);
    List<Token> tokens = scanner.scanTokens();

    if (reporter.hadError) return;

    Parser parser = new Parser(tokens, reporter);
    List<Stmt> statements = parser.parse();

    if (reporter.hadError) return;

    Resolver resolver = new Resolver(interpreter, reporter);
    resolver.resolve(statements);

    if (reporter.hadError) return;

    interpreter.interpret(statements, filename);
  }
}
