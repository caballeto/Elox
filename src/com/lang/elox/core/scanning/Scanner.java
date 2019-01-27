package com.lang.elox.core.scanning;

import com.lang.elox.utils.ErrorReporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.lang.elox.core.scanning.TokenType.*;

public final class Scanner {
  private final ErrorReporter reporter;
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int current = 0, start = 0, line = 1;

  private static final HashMap<String, TokenType> keywords = new HashMap<>();

  static {
    // literals
    keywords.put("true", TRUE);
    keywords.put("false", FALSE);
    keywords.put("null", TOKEN_NULL);

    // types
    keywords.put("Int", TYPE);
    keywords.put("Double", TYPE);
    keywords.put("Boolean", TYPE);
    keywords.put("String", TYPE);
    keywords.put("List", TYPE);
    keywords.put("Dict", TYPE);
    keywords.put("Function", TYPE);
    keywords.put("Lambda", TYPE);
    keywords.put("Object", TYPE);

    // keywords
    keywords.put("if", IF);
    keywords.put("else", ELSE);
    keywords.put("for", FOR);
    keywords.put("while", WHILE);
    keywords.put("def", FUNCTION);
    keywords.put("class", CLASS);
    keywords.put("super", SUPER);
    keywords.put("extends", EXTENDS);
    keywords.put("this", THIS);
    keywords.put("var", VAR);
    keywords.put("return", RETURN);
    keywords.put("try", TRY);
    keywords.put("catch", CATCH);
    keywords.put("throw", THROW);
    keywords.put("new", NEW);
    keywords.put("is", IS);
    keywords.put("lambda", LAMBDA);
    keywords.put("import", IMPORT);
  }

  public Scanner(String source, ErrorReporter reporter) {
    this.source = source;
    this.reporter = reporter;
  }

  public List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();

    switch (c) {
      case '+': addToken(match('=') ? PLUS_EQUAL : PLUS); break;
      case '-': {
        if      (match('=')) addToken(MINUS_EQUAL);
        else if (match('>')) addToken(ARROW);
        else                    addToken(MINUS);
        break;
      }
      case '*': addToken(match('=') ? STAR_EQUAL: STAR); break;
      case '/': {
        if      (match('/')) comment();
        else if (match('*')) multilineComment();
        else if (match('=')) addToken(SLASH_EQUAL);
        else                    addToken(SLASH);
        break;
      }
      case '%': addToken(match('=') ? REMAINDER_EQUAL : REMAINDER); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '>': {
        if      (match('=')) addToken(GREATER_EQUAL);
        else if (match('>')) addToken(BIT_RIGHT);
        else                    addToken(GREATER);
        break;
      }
      case '<': {
        if      (match('=')) addToken(LESS_EQUAL);
        else if (match('<')) addToken(BIT_LEFT);
        else                    addToken(LESS);
        break;
      }
      case '!': addToken(match('=') ? NOT_EQUAL : NOT); break;
      case '&': addToken(match('&') ? AND : BIT_AND); break;
      case '|': addToken(match('|') ? OR : BIT_OR); break;
      case '^': addToken(BIT_XOR); break;
      case '~': addToken(BIT_COMPL); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '[': addToken(LEFT_BRACKET); break;
      case ']': addToken(RIGHT_BRACKET); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ':': addToken(COLON); break;
      case ';': addToken(SEMICOLON); break;
      case '"': string(); break;
      case ' ': case '\r': case '\t': break;
      case '\n': line++; break;

      default: {
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          reporter.syntaxError(line, "Illegal character: " + c);
        }
        break;
      }
    }
  }

  private void number() {
    while (isDigit(peek())) advance();
    boolean isDouble = false;

    if (peek() == '.' && isDigit(peekNext())) {
      isDouble = true;
      advance();
      while (isDigit(peek())) advance();
    }

    if (isDouble) {
      addToken(DOUBLE, Double.parseDouble(source.substring(start, current)));
    } else {
      addToken(INTEGER, Integer.parseInt(source.substring(start, current)));
    }
  }

  private void identifier() {
    while (isAlphaNumeric(peek())) advance();
    String lexeme = source.substring(start, current);
    TokenType type = keywords.get(lexeme);
    addToken((type == null) ? IDENTIFIER : type);
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    if (isAtEnd()) {
      reporter.syntaxError(line, "Unterminated string literal.");
    } else {
      advance();
      addToken(STRING, source.substring(start + 1, current - 1));
    }
  }

  private void comment() {
    while (peek() != '\n' && !isAtEnd()) {
      advance();
    }
  }

  private void multilineComment() {
    while (true) {
      if (isAtEnd()) {
        reporter.syntaxError(line, "Unterminated multiline comment.");
        break;
      }

      if (peek() == '*' && peekNext() == '/') {
        advance();
        advance();
        break;
      }

      advance();
    }
  }

  private boolean match(char c) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != c) return false;

    current++;
    return true;
  }

  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private char advance() {
    current++;
    return source.charAt(current - 1);
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String lexeme = source.substring(start, current);
    tokens.add(new Token(type, lexeme, literal, line));
  }
}