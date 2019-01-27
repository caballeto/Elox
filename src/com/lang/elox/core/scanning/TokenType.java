package com.lang.elox.core.scanning;

public enum TokenType {
  // arithmetic operators

  PLUS,                     // +
  MINUS,                    // -
  STAR,                     // *
  SLASH,                    // /
  REMAINDER,                // %

  // assignment

  EQUAL,                    // =
  PLUS_EQUAL,               // +=
  MINUS_EQUAL,              // -=
  STAR_EQUAL,               // *=
  SLASH_EQUAL,              // /=
  REMAINDER_EQUAL,          // %=

  // relational operators

  EQUAL_EQUAL,               // ==
  GREATER,                   // >
  LESS,                      // <
  NOT_EQUAL,                 // !=
  GREATER_EQUAL,             // >=
  LESS_EQUAL,                // <=

  // logical operators

  AND,                        // &&
  OR,                         // ||
  NOT,                        // !

  // bitwise operations

  BIT_AND,                     // &
  BIT_OR,                      // |
  BIT_XOR,                     // ^
  BIT_COMPL,                   // ~
  BIT_LEFT,                    // <<
  BIT_RIGHT,                   // >>

  // other

  COMMA,                        // ,
  LEFT_PAREN,                   // (
  RIGHT_PAREN,                  // )
  LEFT_BRACKET,                 // ]
  RIGHT_BRACKET,                // [
  LEFT_BRACE,                   // {
  RIGHT_BRACE,                  // }
  //QUESTION,                   // ?
  COLON,                        // :
  SEMICOLON,                    // ;
  ARROW,                        // ->
  DOT,                          // .

  // Literals

  IDENTIFIER,
  STRING,
  INTEGER,
  DOUBLE,
  TRUE,
  FALSE,
  TOKEN_NULL,

  // Keywords

  IF,
  ELSE,
  FOR,
  WHILE,
  FUNCTION,
  CLASS,
  EXTENDS,
  LAMBDA,
  SUPER,
  THIS,
  VAR,
  RETURN,
  TRY,
  CATCH,
  THROW,
  NEW,
  IS,
  IMPORT,
  TYPE,
  EOF
}
