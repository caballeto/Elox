# Elox
Elox is the dynamically-typed language with Python/JavaScript like syntax, based on [Lox language](https://github.com/munificent/craftinginterpreters).
It provides support for new types (**String, List, Dict, Lambda ...**) and methods on these types. 
It also provides new language constructions (see [docs/grammar.txt](https://github.com/caballeto/Elox/blob/master/docs/grammar.txt)), including *try* / *catch*, 
*throw*, *is* operator, bit operations etc.

## Layout
Layout of repository is as follows:
 - `docs/` - simple description on grammar and types
 - `out/` - compiled source
 - `src/` - source code
 - `test/` - some language tests
 - `elox` - sh runner
 
## Setup
Clone repository and run from directory.
```
git clone https://github.com/caballeto/Elox
./elox [script]
```

## Features
### Try/Catch
This construction provides exception handling.
```
try {
  null.length();
} catch (e) {
  writeln(e);
}
```
### Throw
Throw statement provides interface for rising exceptions. Throw can take anything as throwable.
```
class Error {
  def __init__(message) {
    this.message = message;
  }
  
  def getMessage() {
    return this.message;
  }
}

try {
  throw new Error("Custom error!");
} catch (e) {
  writeln(e.getMessage()); // Custom error!
}
```
### Types and native methods
Language provides primitive types, along with some native methods for them.
```
var array = [1, 2, 3, 4, 5];
array.map(lambda: (x) -> { return x*x; }).forEach(lambda: (x) -> {
  writeln("Square: " + x);
});

writeln("Array length: " + array.length());
writeln("IsEmpty: " + array.isEmpty());
```
Language also provides static methods on types itself.
```
writeln(Int.parseInt("99") + 1); // 100
writeln(Double.parseDouble("0.99") + 0.01); // 1.0
writeln("100" == String.toString(100));
```
### Type checking
For type checking language provides `is` operator. See below.
```
writeln(1 is Int); // true
writeln(1.02 is Double); // true
writeln(true is Boolean); // true
writeln("string" is String); // true
writeln([1, 2.5, [1, "string"]] is List); // true
writeln({} is Dict); // true
writeln(lambda: () -> {} is Lambda); // true
```
### Lambda
Lambdas work similar to functions, with difference that they are expressions.
```
var array = [1, 2, 3, 4, 5];
array.map(lambda: (x) -> { return x*x; }).forEach(writeln);
```
Lambdas, as functions, support closures.
```
var getAdder = lambda: (y) -> {
  return lambda: () -> {
    return y += 1;
  };
};

var add = getAdder(0);

writeln(add()); // 1
writeln(add()); // 2
writeln(add()); // 3
```
As lambdas are expression, they can be immediately-invoked.
```
writeln(lambda: () -> { 
  return "Immediately invoked lambda."; 
}());
```
### Inheritance
Code below is the basic exampe of inheritance in Elox.
```
class Quadrilateral {
  def __init__(x, y, z, l) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.l = l;
  }

  def perimeter() {
    return this.x + this.y + this.z + this.l;
  }
}

class Rectangle extends Quadrilateral {
  def __init__(x, y) {
    super.__init__(x, x, y, y);
  }
}

class Square extends Rectangle {
  def __init__(x) {
    super.__init__(x, x);
  }
}

writeln(new Square(5).perimeter()); // 20
```

## More
 - see [test](https://github.com/caballeto/Elox/tree/master/test)
 - see http://www.craftinginterpreters.com/
