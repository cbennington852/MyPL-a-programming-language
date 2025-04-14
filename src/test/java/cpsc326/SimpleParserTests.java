/**
 * CPSC 326, Spring 2025
 * Basic simple-parser tests for HW-2.
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;


/**
 * Test class for the simple parser.
 */
class SimpleParserTests {

  /**
   * Helper to build an input string. 
   */
  InputStream istream(String str) {
    try {
      return new ByteArrayInputStream(str.getBytes("UTF-8")); 
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  //----------------------------------------------------------------------
  // POSITIVE TEST CASES
  
  @Test
  void emptyInput() {
    var p = "";
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void emptyStruct() {
    var p = "struct s {}";
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void FEMTONALY() {
    var p = "void main() { var x1: int = 0 }";
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void emptyFunction() {
    var p = "void f() {}";
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic struct tests

  @Test
  void structWithBaseTypeFields() {
    var p =
      """
      struct my_struct {
        x1: int,
        x2: double,
        x3: bool,
        x4: string
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void structWithNonBaseTypeFields() {
    var p =
      """
      struct my_struct {
        x1: [int],
        x2: my_struct,
        x3: [my_struct]
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic function tests  
  
  @Test
  void functionBaseValueReturnType() {
    var p =
      """
      int my_fun() {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void functionNonBaseValueReturnType() {
    var p =
      """
      my_struct my_fun() {}
      [int] my_fun() {}
      [my_struct] my_fun() {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void functionOneBaseTypeParam() {
    var p =
      """
      void my_fun(x: int) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void functionOneNonBaseTypeParam() {
    var p =
      """
      void my_fun(x: my_struct) {}
      void my_fun(x: [int]) {}
      void my_fun(x: [my_struct]) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void functionWithMultipleBaseTypeParams() {
    var p =
      """
      void my_fun(x1: int, x2: double, x3: string, x4: bool) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void functionWithMulitpleNonBaseTypeParams() {
    var p =
      """
      void my_fun(x1: my_struct, x2: [int], x3: [my_struct]) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic return tests

  @Test
  void returnVariousLiterals() {
    var p =
      """
      int my_fun() {return 10}
      double my_fun() {return 3.14}
      string my_fun() {return "foo"}
      bool my_fun() {return true}
      void my_fun() {return null}
      my_struct my_fun() {return x}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic variable declaration tests

  @Test
  void variableDeclarations() {
    var p =
      """
      void f() {
        var x1: int
        var x2: double
        var x3: bool
        var x4: string
        var x5: my_struct
        var x6: [int]
        var x7: [double]
        var x8: [bool]
        var x9: [string]
        var x10: [my_struct]
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void variableDefinitionsWithTypeAnnotations() {
    var p =
      """
      void f() {
        var x1: int = 42
        var x2: double = 3.14
        var x3: bool = false
        var x4: string = ""
        var x5: my_struct = null
        var x6: [int] = null
        var x7: [double] = null
        var x8: [bool] = null
        var x9: [string] = null
        var x10: [my_struct] = null
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void variableDefinitionsWithOutTypeAnnotations() {
    var p =
      """
      void f() {
        var x1 = 42
        var x2 = 3.14
        var x3 = false
        var x4 = ""
        var x5 = new my_struct()
        var x6 = new int[10]
        var x7 = new double[5]
        var x8 = new bool[x1]
        var x9 = new string[0]
        var x10 = new my_struct[1]
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic if statement tests

  @Test
  void simpleIfStatement() {
    var p =
      """
      void f() {
        if true {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }
  
  @Test
  void simpleIfWithElse() {
    var p =
      """
      void f() {
        if true {}
        else {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void multipleIfElses() {
    var p =
      """
      void f() {
        if true {x = 1}
        else if false {x = 2}
        else if true {x = 3}
        else {x = 4}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void multipleIfs() {
    var p =
      """
      void f() {
        if true {x = 1}
        else if false {x = 2}
        else {x = 3}
        if false {x = 4}
        else {x = 5}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic for loops

  @Test
  void simpleForStatement() {
    var p =
      """
      void f() {
        for i from 0 to 9 {
          x = x + i
        }
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void forStatementWithNonLiteralRange() {
    var p =
      """
      void f() {
        for i from x to y {
          for j from 1 to 10 {
            z = z * (i + j)
          }
        }
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }
  
  // basic while loops

  @Test
  void simpleWhileStatement() {
    var p =
      """
      void f() {
        while true {
          x = x + 1
        }
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }
  
  @Test
  void moreInvolvedWhileStatement() {
    var p =
      """
      void f() {
        var i = 0
        while i < 10 {
          x = x + i
        }
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic path expressions

  @Test
  void basicPathExpressions() {
    var p =
      """
      void f() {
        a.b.c = 0
        x = a.b.c
        xs[0] = 5
        x = xs[1]
        y = xs[0].att1.att2[y].att3
        xs[1].att1.att2[0].att3 = 0
        xs[1].y.z[0].u = xs[0].y.z[0].u.v[1]
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }
  
  // basic function call tests

  @Test
  void basicFunctionCalls() {
    var p =
      """
      void f() {
        z = f1()
        z = f2(42)
        z = f3("foo")
        z = f4(x)
        z = f5(x, y)
        z = f6(x, "foo")
        z = f7(x, y, z)
        z = f8(0, 3.1, "foo", true, null)
        f9(x,y,z) f10() f11(true, null)
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // basic expressions

  @Test
  void basicLogicalExpressions() {
    var p =
      """
      void f() {
        r = x and y or true and not false
        s = not (x and y) and not ((x and z) or y)
        t = (x or not y) and (not x or y) and not not (true or true or false)
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void basicRelationalExpressions() {
    var p =
      """
      void f() {
        r = x == y or (x < y) or (x != y) or (x > y)
        s = not (z or x < y or x > y) and ((x == y) or (x != y))
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void basicArithmeticExpressions() {
    var p =
      """
      void f() {
        r = x + y - z * u / v
        s = ((x + y) / (x - y) + z) / (x * (x - y))
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void basicNewExpressions() {
    var p =
      """
      void f() {
        r = new my_struct(a, b)
        s = new my_struct()
        t = new int[10]
        u = new string[z * (z - 1)]
        v = new my_struct[z + 1]
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  // more involved statments

  @Test
  void nestedStatements() {
    var p =
      """
      void f() {
        if odd(x) {
          while true {
            for i from 0 to x - 1 {
              x = x + 2 + g(x + 2)
            }
          }
        }
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void mixOfStatements() {
    var p =
      """
      void f() {
        var x: int = 1
        x = 2
        if odd(x) {return true}
        while true {x = x + 1}
        for i from 0 to x - 1 {x = x + 2}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }
  
  @Test
  void mixOfStructsAndFunctions() {
    var p =
      """
      struct S1 {x: int}
      void f1() {}
      struct S2 {y: int, z: double}
      int f2(s1: S1, s2: S2) {return s1.x + s2.y}
      string f3() {}
      struct S3 {}
      void main() {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  //----------------------------------------------------------------------
  // NEGATIVE TEST CASES

  // basic tests
  
  @Test
  void statementOutsideFunctionOrStruct() {
    var p =
      """
      var x1: int = 0
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,1] expecting type found 'var'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));
  }

  @Test
  void bracesWithoutStructOrFunction() {
    var p =
      """
      {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,1] expecting type found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // struct tests

  @Test
  void missingStructID() {
    var p =
      """
      struct {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,8] expecting identifier found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingStructOpenBrace() {
    var p =
      """
      struct s x:int}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,10] expecting '{' found 'x'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));  
  }

  @Test
  void missingStructCloseBrace() {
    var p =
      """
      struct s {x:int
      void main() {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,1] expecting '}' found 'void'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingColonInField() { 
    var p =
      """
      struct s {x, y: int}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,12] expecting ':' found ','";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingDataTypeInField() { 
    var p =
      """
      struct s {x: , y: int}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,14] expecting type found ','";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingParameterInField() { 
    var p =
      """
      struct s {x: int, : int}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,19] expecting identifier found ':'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingCommaBetweenFields() { 
    var p =
      """
      struct s {x: int y : int}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,18] expecting '}' found 'y'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // basic function tests

  @Test
  void missingFunctionReturnType() { 
    var p =
      """
      f() {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,2] expecting identifier found '('";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingFunctionOpenParen() { 
    var p =
      """
      int f) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,6] expecting '(' found ')'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingFunctionCloseParen() { 
    var p =
      """
      int f( {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,8] expecting ')' found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));
  }

  @Test
  void missingFunctionOpenBrace() { 
    var p =
      """
      int f() }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,9] expecting '{' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  @Test
  void missingFunctionCloseBrace() { 
    var p =
      """
      int f() {
      void main() {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,1] expecting statement found 'void'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingFunctionParamColon() { 
    var p =
      """
      int f(x, y: int) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,8] expecting ':' found ','";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingFunctionParamType() { 
    var p =
      """
      int f(x: , y: int) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,10] expecting type found ','";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  @Test
  void missingFunctionParamID() { 
    var p =
      """
      int f(x: int, ) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,15] expecting identifier found ')'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingFunctionParamComma() { 
    var p =
      """
      int f(x: int y: bool) {}
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,14] expecting ')' found 'y'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // basic return statements

  @Test
  void returnWithoutExpression() { 
    var p =
      """
      void f() { return }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,19] expecting identifier found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // basic variable statements

  @Test
  void variableDeclWithoutVar() {
    var p =
      """
      void f() { x: int }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,13] expecting '=' found ':'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void variableDeclWithMissingColon() {
    var p =
      """
      void f() { var x int }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,18] expecting ':' found 'int'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  @Test
  void variableDeclWithoutIdentifier() {
    var p =
      """
      void f() { var int x }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,16] expecting identifier found 'int'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void variableDeclWithoutType() {
    var p =
      """
      void f() { var x: = 0 }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,19] expecting type found '='";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void variableDefinitionMissingExpression() {
    var p =
      """
      void f() { var x: int =  }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,26] expecting identifier found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // basic if statements

  @Test
  void missingIfExpression() {
    var p =
      """
      void f() { if {} }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,15] expecting identifier found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingIfBody() {
    var p =
      """
      void f() {
        if true
        x = 5
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,3] expecting '{' found 'x'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void nonTerminatingIfBody() {
    var p =
      """
      void f() {
        if true {
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [4,1] expecting statement found 'end-of-stream'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  @Test
  void elseWithNoIf() {
    var p =
      """
      void f() {
        else {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,3] expecting statement found 'else'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void elseIfWithNoIf() {
    var p =
      """
      void f() {
        else if true {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,3] expecting statement found 'else'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // Special case -- for me
  @Test
  void specialCaseLargeFile() {
    var p = """
        #----------------------------------------------------------------------
        # MyPL file to test different parts of the parser.
        # Only checks for valid syntax examples. See unit tests for additional
        # test cases.
        #----------------------------------------------------------------------


        struct EmptyNode {
        }

        struct Node {
          value: int,
          next: Node
        }

        struct KVNode {
          key: string,
          value: int,
          next: KVNode
        }

        struct VariousTypesTest {
          v: int,
          w: double,
          x: bool,
          z: string,
          a: [int]
        }


        void f1() {
          return null
        }

        int f2() {
          return 42
        }

        void f3() {
          var x1: int = 42
          var x2: double = 3.14
          var x3: bool = true
          var x4: string = "abc"
          var x5: Node = new Node(x1, null)

          var x6: int
          var x7: double
          var x8: bool
          var x9: string
          var x10: Node

          var x11 = 42
          var x12 = 3.14
          var x13 = true
          var x14 = "abc"
          var x15 = new Node(x11, null)

          var x16: int = null
          var x17: double = null
          var x18: string = null
          var x19: Node = null
        }

        void f4(p1: int) {
          if true {
          }
          else {
          }
        }

        int f5(p1: bool) {
          if p1 {
            return 42
          }
          return 43
        }

        int f6(p1: int, p2: int) {
          if (p1 - p2) < p1 {
            return p1
          }
          else if p2 >= p1 {
            return p2
          }
          else {
            return p1 + p2
          }
        }

        Node f7(v1: int, msg: string) {
          if v1 >= 1 {
            print(msg)
            return new Node(v1, null)
          }
          return null
        }

        bool f8(p1: int, p2: double, p3: bool, p4: char, p5: string) {
          for i from 1 to 10 {
            if (even(i)) {
              for j from 1 to j {
                if (not even(j)) {
                  print(i)
                }
              }
            }
          }
          return true
        }

        string f9(n: int) {
          var s: string = ""
          while n > 0 {
            s = s + "a"
            n = n + 1
          }
          return s
        }

        double f10(x: double, y: double) {
          var r: double = 0.0
          if (x < y or x == y) and x > 0 {
            r = ((x / (x + y)) * 100) + (x * 0.1)
          }
          else if y > 0 {
            r = ((y / (x + y)) * 100) + (y * 0.1)
          }
          else {
            r = 100
          }
          return r
        }

        int f11(x: int) {
          if x < 1 {
            return 0 - 1
          }
          else if x == 1 {
            return 1
          }
          return f11(x-1) + f11(x-2)
        }

        Node f12(node: Node) {
          if node == null {
            return null
          }
          var copy = new Node(node.val, f12(node.next))
          if (copy.next == null) {
            copy.next = f12(node)
          }
          return copy
        }

        void f13(node: Node) {
          if node != null {
            f13(node.next)
          }
        }

        int f14(i: int, a: [int]) {
          if i < 0 or i >= a.size {
            return null
          }
          return a[i]
        }

        [int] f15(a: [int]) {
          var copy: [int] = new int[a.length / 2]
          for i from 0 to (a.length / 2) - 1 {
            copy[i] = a[i]
          }
          return copy
        }
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }



  @Test
  void elseWithNoBody() {
    var p =
      """
      void f() {
        if true {} else 
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting '{' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void elseWithNoTerminatingBody() {
    var p =
      """
      void f() {
        if true {} else {
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [4,1] expecting statement found 'end-of-stream'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  @Test
  void elseOutOfOrder() {
    var p =
      """
      void f() {
        if true {}
        else {}
        else if false {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [4,3] expecting statement found 'else'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  // basic while statements

  @Test
  void missingWhileExpression() {
    var p =
      """
      void f() { while {} }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [1,18] expecting identifier found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void missingWhileBody() {
    var p =
      """
      void f() {
        while true
        x = 5
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,3] expecting '{' found 'x'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void nonTerminatingWhileBody() {
    var p =
      """
      void f() {
        while true {
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [4,1] expecting statement found 'end-of-stream'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  // basic for statements

  @Test
  void forWithoutLoopInformation() {
    var p =
      """
      void f() {
        for {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,7] expecting identifier found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void forWithoutLoopFrom() {
    var p =
      """
      void f() {
        for i {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,9] expecting 'from' found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void forWithoutLoopFromExpression() {
    var p =
      """
      void f() {
        for i from to {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,14] expecting identifier found 'to'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void forWithoutLoopTo() {
    var p =
      """
      void f() {
        for i from 0 {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,16] expecting 'to' found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void forWithoutLoopToExpression() {
    var p =
      """
      void f() {
        for i from 0 to {}
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,19] expecting identifier found '{'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void forWithoutLoopBody() {
    var p =
      """
      void f() {
        for i from 0 to 10
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting '{' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void forWithNonTerminatedLoopBody() {
    var p =
      """
      void f() {
        for i from 0 to 10 {
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [4,1] expecting statement found 'end-of-stream'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // basic assignment statements

  @Test
  void assignWithMissingExpression() {
    var p =
      """
      void f() {
        x = 
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting identifier found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
    
  @Test
  void assignWithMissingAssignOp() {
    var p =
      """
      void f() {
        x 0 
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,5] expecting '=' found '0'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void assignWithBadNonArrayLvalue() {
    var p =
      """
      void f() {
        x.y. = 0
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,8] expecting identifier found '='";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }
  
  @Test
  void assignWithBadArrayLvalue() {
    var p =
      """
      void f() {
        x.y[10 = 0
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,10] expecting ']' found '='";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  // simple expressions

  @Test
  void expressionWithMissingOperand() {
    var p =
      """
      void f() {
        x = 2.1 * 4 + 
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting identifier found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void expressionWithTooManyOperands() {
    var p =
      """
      void f() {
        x = 2.1 * + 4
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,13] expecting identifier found '+'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void expressionWithTooFewClosingParens() {
    var p =
      """
      void f() {
        x = (2.1 * 3.0
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting ')' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void expressionWithTooFewOpeningParens() {
    var p =
      """
      void f() {
        x = (2.1 * 3.0) + 7)
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [2,22] expecting statement found ')'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void expressionWithBadNonArrayIDRvalue() {
    var p =
      """
      void f() {
        x = y.z.
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting identifier found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void expressionWithBadArrayIDRvalue() {
    var p =
      """
      void f() {
        x = y.z[10
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting ']' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  //----------------------------------------------------------------------
  // TODO: Design and implement the following unit tests and add them
  // below. Make sure your added unit tests pass.
  //
  // 1. Three new "positive" tests. Each test should involve an
  //    "interesting" syntax case.
  //
  // 2. Two new "negative" tests. Both tests should involve an
  //    "interesting" syntax error.
  // 
  //----------------------------------------------------------------------  



  //Positive Tests
  //1.
  @Test
  void exampleTestCase1() {
    var p = """
        void f() {
          print(y.z[null])
        }
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  //2.
  @Test
  void exampleTestCase2() {
    var p = """
        int f14(i: int, a: [int]) {
          if i < 0 or i >= a.size { #i love if statements!!
            return null
          }
          return a[i]
        }

        struct Node {
          value: int, #giggle
          next: Node #is a node
        }

        #bouncy balls
        ##################
        #Cheese burder

        int main () {
          print("Hello cheese")
        }
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  //3.
  @Test
  void exampleTestOnlyComment() {
    var p = """
        #only comment
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  //4.
  @Test
  void exampleTestLotsOfDots() {
    var p = """
        void f() {
          x = y.zk.i.y.r.erg.ge.rg.erg.fo
        }
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }


  //Negative tests
  //5.
  @Test
  void expressionWithBadArrayIDValue() {
    var p = """
        void f() {
          x = y.z[for]
        }
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting ']' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));
  }

  //6.
  @Test
  void exampleNegativeTestVoidInArray() {
    var p = """
        void f() {
          print(y.z[void])
        }
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting ']' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));
  }

  //7.
  @Test
  void exampleNegativeTest() {
    var p = """
        void f() {
          return void
        }
        """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    // var m = "PARSE_ERROR: [3,1] expecting ']' found '}'";
    // assertEquals(m, e.getMessage());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));
  }


}

 
