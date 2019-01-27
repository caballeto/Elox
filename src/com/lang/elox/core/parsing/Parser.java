package com.lang.elox.core.parsing;

import com.lang.elox.core.scanning.Scanner;
import com.lang.elox.core.scanning.Token;
import com.lang.elox.core.scanning.TokenType;
import com.lang.elox.errors.CircularImportError;
import com.lang.elox.errors.RuntimeError;
import com.lang.elox.representation.parsetime.Expr;
import com.lang.elox.representation.parsetime.Stmt;
import com.lang.elox.utils.ErrorReporter;
import com.lang.elox.utils.ImportValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.lang.elox.core.scanning.TokenType.*;

public final class Parser {
  private static class ParseError extends RuntimeException {}
  private static ImportValidator validator = new ImportValidator();

  private final ErrorReporter reporter;
  private final List<Token> tokens;
  private int current = 0;

  public Parser(List<Token> tokens, ErrorReporter reporter) {
    this.tokens = tokens;
    this.reporter = reporter;
  }

  public static ImportValidator getValidator() {
    return validator;
  }

  public List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(IMPORT))   return importDeclaration();
      if (match(CLASS))    return classDeclaration();
      if (match(FUNCTION)) return function();
      if (match(VAR))      return varDeclaration();
      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt importDeclaration() {
    Token name = consume(STRING, "Expected module name.");
    consume(SEMICOLON, "Expected ';' after import statement.");

    try {
      String filename = (String) name.literal;
      int index = filename.lastIndexOf('/');
      if (index != -1) {
        validator.path.add(filename.substring(0, index + 1));
        filename = filename.substring(index + 1);
      }

      // get relative path from running directory
      String pathToFile = validator.prefix() + filename;

      // check for circular import, add to current import path
      validator.validateFile(pathToFile);
      validator.add(pathToFile);

      // Read and parse imported file
      String source = new String(Files.readAllBytes(Paths.get(pathToFile)), StandardCharsets.UTF_8);
      List<Stmt> statements = new Parser(new Scanner(source, reporter).scanTokens(), reporter).parse();

      // delete from current import path
      validator.pop();
      if (!validator.path.isEmpty()) validator.path.remove(validator.path.size() - 1);

      return new Stmt.Import(name, statements);
    } catch (IOException e) {
      throw error(name, "RuntimeError: No such file exception.");
    } catch (RuntimeError e) {
      throw error(name, "RuntimeError: while importing file.");
    } catch (CircularImportError e) {
      error(new Token(name.type, (String) name.literal, null, name.line), "Circular import error. ");
      reporter.circularImportError(e.cycle());
      validator.pop();
      if (!validator.path.isEmpty()) validator.path.remove(validator.path.size() - 1);
    }

    return null;
  }

  private Stmt classDeclaration() {
    Token name = consume(IDENTIFIER, "Expected class name.");

    Expr.Variable superclass = null;
    if (match(EXTENDS)) {
      consume(IDENTIFIER, "Expected superclass name.");
      superclass = new Expr.Variable(previous());
    }

    consume(LEFT_BRACE, "Expected '{' after class declaration.");

    List<Stmt.Function> methods = new ArrayList<>();
    while (match(FUNCTION) && !check(RIGHT_BRACE) && !isAtEnd()) {
      methods.add(function());
    }

    consume(RIGHT_BRACE, "Expected '}' after class body.");
    return new Stmt.Class(name, superclass, methods);
  }

  private Stmt.Function function() {
    Token name = consume(IDENTIFIER, "Expect function name.");
    consume(LEFT_PAREN, "Expect '(' after function name.");
    List<Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 8) {
          error(peek(), "Cannot have more than 8 parameters.");
        }

        parameters.add(consume(IDENTIFIER, "Expect parameter name."));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Expect ')' after parameters.");
    consume(LEFT_BRACE, "Expect '{' before function body.");
    List<Stmt> body = block();
    return new Stmt.Function(name, parameters, body);
  }

  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expected variable name.");

    Expr initializer = null;
    if (match(EQUAL)) {
      initializer = expression();
    }

    if (check(PLUS_EQUAL) || check(STAR_EQUAL) || check(SLASH_EQUAL) || check(MINUS_EQUAL) ||check(REMAINDER_EQUAL)) {
      throw error(peek(), "Invalid variable initialization operator.");
    }

    consume(SEMICOLON, "Expected ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(THROW)) return throwStatement();
    if (match(TRY)) return tryStatement();
    if (match(RETURN)) return returnStatement();
    if (match(IF)) return ifStatement();
    if (match(FOR)) return forStatement();
    if (match(WHILE)) return whileStatement();
    if (match(LEFT_BRACE)) return new Stmt.Block(block());
    return expressionStatement();
  }

  private Stmt throwStatement() {
    Token token = previous();
    Expr expr = expression();
    consume(SEMICOLON, "Expected ';' after throw statement.");
    return new Stmt.Throw(token, expr);
  }

  private Stmt tryStatement() {
    consume(LEFT_BRACE, "Expected '{' after try.");
    List<Stmt> tryStmt = block();
    consume(CATCH, "Expected 'catch' block.");

    consume(LEFT_PAREN, "Expected '(' after catch keyword.");
    Token parameter = consume(IDENTIFIER, "Expected catch parameter.");
    consume(RIGHT_PAREN, "Expected ')' after catch parameter.");

    consume(LEFT_BRACE, "Expected '{' after catch.");
    List<Stmt> catchStmt = block();
    return new Stmt.Try(parameter, tryStmt, catchStmt);
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;
    if (!check(SEMICOLON)) {
      value = expression();
    }

    consume(SEMICOLON, "Expect ';' after value.");
    return new Stmt.Return(keyword, value);
  }

  private Stmt forStatement() {
    consume(LEFT_PAREN, "Expected '(' after for.");

    Stmt initializer;
    if (match(SEMICOLON)) {
      initializer = null;
    } else if (match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;
    if (!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expected ';' after loop condition.");

    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expected ')' after for loop.");
    Stmt body = statement();

    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
    }

    if (condition == null) condition = new Expr.Literal(true);
    body = new Stmt.While(condition, body);

    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expected '(' after 'while'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expected ')' after 'while'.");
    Stmt body = statement();
    return new Stmt.While(condition, body);
  }

  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Expected '(' after 'if'.");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expected ')' after condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();
    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(RIGHT_BRACE, "Expected '}' after block.");
    return statements;
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(SEMICOLON, "Expected ';' after expression.");
    return new Stmt.Expression(expr);
  }

  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = logicOr();

    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      } else if (expr instanceof Expr.Get) {
        Expr.Get get = (Expr.Get) expr;
        return new Expr.Set(get.object, get.name, value);
      } else if (expr instanceof Expr.IndexGet) {
        Expr.IndexGet get = (Expr.IndexGet) expr;
        return new Expr.IndexSet(get.object, get.index, value, get.token);
      }

      throw error(equals, "Invalid assignment target.");
    }

    if (match(PLUS_EQUAL)) {
      Token plus_equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token operator = new Token(PLUS, "+", null, ((Expr.Variable) expr).name.line);
        Token name = ((Expr.Variable) expr).name;
        Expr binary = new Expr.Binary(expr, operator , value);
        return new Expr.Assign(name, binary);
      } else if (expr instanceof Expr.Get) {
        Token operator = new Token(PLUS, "+", null, ((Expr.Get) expr).name.line);
        Expr.Get get = (Expr.Get) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.Set(get.object, get.name, binary);
      } else if (expr instanceof Expr.IndexGet) {
        Token operator = new Token(PLUS, "+", null, ((Expr.IndexGet) expr).token.line);
        Expr.IndexGet get = (Expr.IndexGet) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.IndexSet(get.object, get.index, binary, get.token);
      }

        throw error(plus_equals, "Invalid assignment target.");
    }

    if (match(MINUS_EQUAL)) {
      Token minus_equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token operator = new Token(MINUS, "-", null, ((Expr.Variable) expr).name.line);
        Token name = ((Expr.Variable) expr).name;
        Expr binary = new Expr.Binary(expr, operator , value);
        return new Expr.Assign(name, binary);
      } else if (expr instanceof Expr.Get) {
        Token operator = new Token(MINUS, "-", null, ((Expr.Get) expr).name.line);
        Expr.Get get = (Expr.Get) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.Set(get.object, get.name, binary);
      } else if (expr instanceof Expr.IndexGet) {
        Token operator = new Token(MINUS, "-", null, ((Expr.IndexGet) expr).token.line);
        Expr.IndexGet get = (Expr.IndexGet) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.IndexSet(get.object, get.index, binary, get.token);
      }

      throw error(minus_equals, "Invalid assignment target.");
    }

    if (match(STAR_EQUAL)) {
      Token star_equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token operator = new Token(STAR, "*", null, ((Expr.Variable) expr).name.line);
        Token name = ((Expr.Variable) expr).name;
        Expr binary = new Expr.Binary(expr, operator , value);
        return new Expr.Assign(name, binary);
      } else if (expr instanceof Expr.Get) {
        Token operator = new Token(STAR, "*", null, ((Expr.Get) expr).name.line);
        Expr.Get get = (Expr.Get) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.Set(get.object, get.name, binary);
      } else if (expr instanceof Expr.IndexGet) {
        Token operator = new Token(STAR, "*", null, ((Expr.IndexGet) expr).token.line);
        Expr.IndexGet get = (Expr.IndexGet) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.IndexSet(get.object, get.index, binary, get.token);
      }

      throw error(star_equals, "Invalid assignment target.");
    }

    if (match(SLASH_EQUAL)) {
      Token slash_equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token operator = new Token(SLASH, "/", null, ((Expr.Variable) expr).name.line);
        Token name = ((Expr.Variable) expr).name;
        Expr binary = new Expr.Binary(expr, operator , value);
        return new Expr.Assign(name, binary);
      } else if (expr instanceof Expr.Get) {
        Token operator = new Token(SLASH, "/", null, ((Expr.Get) expr).name.line);
        Expr.Get get = (Expr.Get) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.Set(get.object, get.name, binary);
      } else if (expr instanceof Expr.IndexGet) {
        Token operator = new Token(SLASH, "/", null, ((Expr.IndexGet) expr).token.line);
        Expr.IndexGet get = (Expr.IndexGet) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.IndexSet(get.object, get.index, binary, get.token);
      }

      throw error(slash_equals, "Invalid assignment target.");
    }

    if (match(REMAINDER_EQUAL)) {
      Token remainder_equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token operator = new Token(REMAINDER, "%", null, ((Expr.Variable) expr).name.line);
        Token name = ((Expr.Variable) expr).name;
        Expr binary = new Expr.Binary(expr, operator , value);
        return new Expr.Assign(name, binary);
      } else if (expr instanceof Expr.Get) {
        Token operator = new Token(REMAINDER, "%", null, ((Expr.Get) expr).name.line);
        Expr.Get get = (Expr.Get) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.Set(get.object, get.name, binary);
      } else if (expr instanceof Expr.IndexGet) {
        Token operator = new Token(REMAINDER, "%", null, ((Expr.IndexGet) expr).token.line);
        Expr.IndexGet get = (Expr.IndexGet) expr;
        Expr binary = new Expr.Binary(expr, operator, value);
        return new Expr.IndexSet(get.object, get.index, binary, get.token);
      }

      throw error(remainder_equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr logicOr() {
    Expr expr = logicAnd();

    while (match(OR)) {
      Token operator = previous();
      Expr right = logicAnd();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr logicAnd() {
    Expr expr = bitwiseOr();

    while (match(AND)) {
      Token operator = previous();
      Expr right = bitwiseOr();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwiseOr() {
    Expr expr = bitwiseXor();

    while (match(BIT_OR)) {
      Token operator = previous();
      Expr right = bitwiseXor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwiseXor() {
    Expr expr = bitwiseAnd();

    while (match(BIT_XOR)) {
      Token operator = previous();
      Expr right = bitwiseAnd();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwiseAnd() {
    Expr expr = equality();

    while (match(BIT_AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(NOT_EQUAL, EQUAL_EQUAL, IS)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = shift();

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = shift();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr shift() {
    Expr expr = addition();

    while (match(BIT_LEFT, BIT_RIGHT)) {
      Token operator = previous();
      Expr right = addition();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr addition() {
    Expr expr = multiplication();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = multiplication();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr multiplication() {
    Expr expr = unary();

    while (match(SLASH, STAR, REMAINDER)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(NOT, MINUS, BIT_COMPL)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }
    return invocation();
  }

  private Expr invocation() {
    if      (match(NEW))    return instantiation();
    else if (match(LAMBDA)) return lambda();
    else                    return call();
  }

  private Expr instantiation() {
    Expr expr = primary();

    consume(LEFT_PAREN, "Expected '(' after class instantiation.");
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 8) {
          error(peek(), "Cannot have more than 8 arguments.");
        }
        arguments.add(expression());
      } while (match(COMMA));
    }

    Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments.");
    expr = new Expr.Instance(expr, paren, arguments);

    while (true) {
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(DOT)) {
        Token name = consume(IDENTIFIER, "Expected property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else if (match(LEFT_BRACKET)){
        Expr index = expression();
        Token token = consume(RIGHT_BRACKET, "Expected ']' after index call.");
        expr = new Expr.IndexGet(expr, index, token);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr lambda() {
    Token lambda = previous();
    consume(COLON, "Expected ':' in lambda declaration.");
    consume(LEFT_PAREN, "Expected parameter list in lambda declaration.");

    List<Token> parameters = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 8) {
          throw error(peek(), "Cannot have more than 8 parameters in lambda.");
        }
        parameters.add(consume(IDENTIFIER, "Expect parameter name."));
      } while (match(COMMA));
    }

    consume(RIGHT_PAREN, "Expect ')' after parameters.");
    consume(ARROW, "Expect '->' in lambda expression.");
    consume(LEFT_BRACE, "Expect '{' before lambda body.");

    List<Stmt> body = block();
    Expr expr = new Expr.Lambda(lambda, parameters, body);

    while (true) {
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(DOT)) {
        Token name = consume(IDENTIFIER, "Expected property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else if (match(LEFT_BRACKET)){
        Expr index = expression();
        Token token = consume(RIGHT_BRACKET, "Expected ']' after index expression.");
        expr = new Expr.IndexGet(expr, index, token);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr call() {
    Expr expr = primary();

    while (true) {
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else if (match(DOT)) {
        Token name = consume(IDENTIFIER, "Expected property name after '.'.");
        expr = new Expr.Get(expr, name);
      } else if (match(LEFT_BRACKET)){
        Expr index = expression();
        Token token = consume(RIGHT_BRACKET, "Expected ']' after index expression.");
        expr = new Expr.IndexGet(expr, index, token);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr primary() {
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TOKEN_NULL)) return new Expr.Literal(null);
    if (match(INTEGER, DOUBLE)) return new Expr.Literal(previous().literal);
    if (match(STRING)) return new Expr.StringLiteral((String) previous().literal);
    if (match(TYPE)) return new Expr.TypeLiteral(previous());
    if (match(IDENTIFIER)) return new Expr.Variable(previous());
    if (match(THIS)) return new Expr.This(previous());

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expected ')' after expression.");
      return new Expr.Grouping(expr);
    }

    if (match(LEFT_BRACKET)) {
      List<Expr> elements = new ArrayList<>();
      if (!check(RIGHT_BRACKET)) {
        do {
          elements.add(expression());
        } while (match(COMMA));
      }

      Token paren = consume(RIGHT_BRACKET, "Expected ']' after list initialization.");
      return new Expr.ListLiteral(paren, elements);
    }

    if (match(LEFT_BRACE)) {
      Map<Expr, Expr> map = new HashMap<>();
      if (!check(RIGHT_BRACE)) {
        do {
          Expr key = expression();
          consume(COLON, "Expected ':' between key, value.");
          Expr value = expression();
          map.put(key, value);
        } while (match(COMMA));
      }
      Token paren = consume(RIGHT_BRACE, "Expected '}' after map definition.");
      return new Expr.DictLiteral(paren, map);
    }

    if (match(SUPER)) {
      Token keyword = previous();
      consume(DOT, "Expectde '.' after 'super'.");
      Token method = consume(IDENTIFIER, "Expected superclass name.");
      return new Expr.Super(keyword, method);
    }


    throw error(peek(), "Expression expected.");
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();
    if (!check(RIGHT_PAREN)) {
      do {
        arguments.add(expression());
      } while (match(COMMA));
    }

    Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
    return new Expr.Call(callee, paren, arguments);
  }

  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();
    throw error(peek(), message);
  }

  private ParseError error(Token token, String message) {
    reporter.error(token, message);
    return new ParseError();
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType type) {
    if (isAtEnd()) return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUNCTION:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case RETURN:
        case IMPORT:
        case TRY:
        case THROW:
          return;
      }

      advance();
    }
  }
}