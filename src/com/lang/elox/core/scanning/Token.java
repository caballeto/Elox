package com.lang.elox.core.scanning;

public class Token {
  public final int line;
  public final String lexeme;
  public final Object literal;
  public final TokenType type;

  public Token(TokenType type, String lexeme, Object literal, int line) {
    this.line = line;
    this.lexeme = lexeme;
    this.literal = literal;
    this.type = type;
  }

  @Override
  public String toString() {
    return "[" + type + " : '" + lexeme + "' ]";
  }
}
