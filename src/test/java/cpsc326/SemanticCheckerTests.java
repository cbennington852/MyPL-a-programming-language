/**
 * CPSC 326, Spring 2025
 * Basic semantic checker unit tests.
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;


/**
 * Unit tests for the SemanticChecker implementation
 */
class SemanticCheckerTests {

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
  // Basic Function Definitions
  
  @Test
  void smallestValidProgram() {
    var p = "void main() {}";
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void validFunctionDefs() {
    var p =
      """
      void f1(x: int) {}
      void f2(x: double) {}
      bool f3(x: bool) {}
      string f4(p1: int, p2: bool) {}
      void f5(p1: double, p2: int, p3: string) {}
      int f6(p1: int, p2: int, p3: string) {}
      [int] f7() {}
      void main() {}
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void missingMain() {
    var p = "";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void mainWithParams() {
    var p = "void main(x: int) {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void mainWithWrongReturnType() {
    var p = "int main() {}";
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void redefinedBuiltIn() {
    var p =
      """
      void readln(msg: string) {}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void twoFunctionsWithSameName() {
    var p =
      """
      void f(msg: string) {}
      int f() {}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void functionWithTwoSameNamedParams() {
    var p =
      """
      void f(x: int, y: double, x: string) {}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void functionWithBadParameterType() {
    var p =
      """
      void f(x: int, y: node) {}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void functionWithBadArrayParameterType() {
    var p =
      """
      void f(x: [node]) {}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void functionWithBadReturnType() {
    var p =
      """
      node f(x: int) {}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void functionWithBadArrayReturnType() {
    var p =
      """
      [node] f(x: int) {}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Basic Struct Definitions
  
  @Test
  void validStructDefs() {
    var p =
      """
      struct s1 {x: int, y: int}
      struct s2 {x: bool, y: string, z: double}
      struct s3 {s1_val: s1}
      struct s4 {xs: [int]}
      struct s5 {s4s: [s4]}
      void main() {}
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void structSelfReference() {
    var p =
      """
      struct node {val: int, next: node}
      struct big_node {val: [int], children: [big_node]}
      void main() {}
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }
  
  @Test
  void structsMutualReference() {
    var p =
      """
      struct s1 {x: s2, v: int}
      struct s2 {x: s1, v: int}
      void main() {}
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void structAndFunctionWithSameName() {
    var p =
      """
      struct s {}
      void s() {}
      void main() {}
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void functionWithStructParam() {
    var p =
      """
      void f(x: int, y: s, z: [s]) {}
      struct s {}
      void main() {}
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void twoStructsWithSameName() {
    var p =
      """
      struct s {x: int}
      struct s {y: bool}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void structWithUndefinedFieldType() {
    var p =
      """
      struct s1 {x: int, s: s2}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void structWithTwoFieldsHavingSameName() {
    var p =
      """
      struct s {x: int, y: double, x: string}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Variable decleration statements
  
  @Test
  void validFullBaseTypeVarStatements() {
    var p =
      """
      void main() {
        var x1: int = 0
        var x2: double = 0.0
        var x3: string = "foo"
        var x4: bool = true
        var x5: int = null
        var x6: double = null
        var x7: string = null
        var x8: bool = null
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void validFullBaseTypeArrayVarStatements() {
    var p =
      """
      void main() {
        var x1: [int] = null
        var x2: [double] = null
        var x3: [string] = null
        var x4: [bool] = null
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }
  
  @Test
  void validInferedTypeVarStatements() {
    var p =
      """
      void main() {
        var x1 = 0
        var x2 = 0.0
        var x3 = "foo"
        var x4 = true
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void validVarStatementsWithoutDefs() {
    var p =
      """
      void main() {
        var x1: int
        var x2: double
        var x3: string
        var x4: bool
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void localShadow() {
    var p =
      """
      void main() {
        var x1: int
        var x2: double
        var x1: bool
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void mismatchedNonArrayIntVarStatementTypes() {
    var p =
      """
      void main() {
        var x1: int = 3.14
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void mismatchedNonArrayDoubleVarStatementTypes() {
    var p =
      """
      void main() {
        var x1: double = 0
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void mismatchedArrayIntVarStatementTypes() {
    var p =
      """
      void main() {
        var x1: [int] = 256
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void unableToInferType() {
    var p =
      """
      void main() {
        var x1 = null
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Expressions
  
  @Test
  void exprWithNoParens() {
    var p =
      """
      void main() {
        var x1: int = 1 + 2 + 3 * 4 / 5 - 6 - 7
        var x2: double = 1.0 + 2.1 + 3.3 * 4.4 / 5.5 - 6.6 - 7.7
        var x3: bool = not true or false and true and not false
        var x4: string = "a" + "b" + "c"
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void exprWithParens() {
    var p =
      """
      void main() {
        var x1: int = ((1 + 2) + (3 * 4)) / ((5 - 6) - 7)
        var x2: double = ((1.0 + 2.1) + (3.3 * 4.4) / (5.5 - 6.6)) - 7.7
        var x3: bool = not (true or false) and (true and not false)
        var x4: string = (("a" + "b") + "c")
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void exprWithParensAndVars() {
    var p =
      """
      void main() {
        var x1: int = (1 + 2) + (3 * 4) 
        var x2: int = (5 - 6) - 7 
        var x3: int = ((x1 / x2) + x1 - x2) / (x1 + x2)  
        var x4: double = (1.0 + 2.1) + (3.3 * 4.4) 
        var x5: double = (5.5 - 6.6) - 7.7 
        var x6: double = ((x4 / x5) + x5 - x4) / (x4 + x5) 
        var x7: bool = not (true or false) 
        var x8: bool = true and not x7 
        var x9: bool = (x7 and x8) or (not x7 and x8) or (x7 and not x8) 
        var x10: string = "a" + "b" 
        var x11: string = (x10 + "c") + ("c" + x10) 
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void basicRelationalOperators() {
    var p =
      """
      void main() {
        var x1: bool = 0 < 1
        var x2: bool = 0 <= 1 
        var x3: bool = 0 > 1
        var x4: bool = 0 >= 1
        var x5: bool = 0 != 1
        var x6: bool = 0 == 1
        var x7: bool = 0 != null
        var x8: bool = 0 == null
        var x9: bool = null != null
        var x10: bool = null == null
        var x11: bool = not 0 < 1
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void combinedRelationalOperators() {
    var p =
      """
      void main() {
        var x1: bool = (0 < 1) and ("a" < "b") and (3.1 < 3.2)
        var x2: bool = (not ("a" == null)) or (not (3.1 != null))
        var x3: bool = ("abc" <= "abde") or (x1 == false)
        var x4: bool = (not x2 == null) and 3.1 >= 4.1
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void arrayComparisons() {
    var p =
      """
      void main() {
      var x1: [int] = new int[10]
      var x2: [int] = x1
      var x3: bool = (x2 != null) and ((x1 != x2) or (x1 == x2)) 
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void illegalNonArrayRelationalComparison() {
    var p =
      """
      void main() {
        var x1: bool = true < false
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void illegalArrayRelationalComparison() {
    var p =
      """
      void main() {
        var x1: [int] = new int[10]
        var x2: [int] = x1
        var x3: bool = x1 <= x2
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalArrayWithAddition() {
    var p =
      """
      void main() {
        var x1: [int] = new int[10]
        var x2: [int] = new int[10]
        var x3: [int] = x1 + x2
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void illegalLogicalNegationWithInt() {
    var p =
      """
      void main() {
        var x: bool = not (1 + 2)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalLogicalAndWithInt() {
    var p =
      """
      void main() {
        var x = true and 0
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void illegalLogicalOrWithString() {
    var p =
      """
      void main() {
        var x = false or (true or "false")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalMultiplication() {
    var p =
      """
      void main() {
        var x = 3.0 * 0
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalAdditionUsingBools() {
    var p =
      """
      void main() {
        var x = true + false
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalSubtractionWithNull() {
    var p =
      """
      void main() {
        var x = 10 - null
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalAdditionWithIntAndArray() {
    var p =
      """
      void main() {
        var xs: [int] = new int[10]
        var x: int = 10
        var y: int = x + xs
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Function return statements

  @Test
  void basicFunctionReturnStatements() {
    var p =
      """
      int f() {return 32}
      int g() {return null}
      void h() {return null}
      bool i() {return true}
      [double] j() {return new double[10]}
      [string] k() {return null}
      int l(x: int) {return x}
      [int] m(ys: [int]) {return ys}
      void main() {}
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void illegalNonNullReturn() {
    var p =
      """
      void main() {
        return 0
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void oneIllegalExecutionPath() {
    var p =
      """
      int f(x: int) {
        if x < 0 {return 0}
        else {return false}
      }
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // If Statements

  @Test
  void basicIfStatements() {
    var p =
      """
      void main() {
        if true {}
        if false {} else {}
        if false {} else if true {}
        if true {} else if false {} else {}
        if false {} else if false {} else if true {} else {}
        if true { if false {} else if true { if false {} else {} } } else {}
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void illegalNonBoolIfCondition() {
    var p =
      """
      void main() {
        if 1 {}
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalNonBoolElseIfCondition() {
    var p =
      """
      void main() {
        if false {} else if "true" {}
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalBoolArrayIfCondition() {
    var p =
      """
      void main() {
        var flags: [bool] = new bool[2]
        if flags {} 
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalNullIfCondition() {
    var p =
      """
      void main() {
        if null {} 
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // While Statements

  @Test
  void basicWhileStatements() {
    var p =
      """
      void main() {
        while true {}
        while false {}
        var flag: bool
        while flag {}
        while true {while not flag {}}
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void illegalIntWhileCondition() {
    var p =
      """
      void main() {
        while 3 * 2 {} 
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalBoolArrayWhileCondition() {
    var p =
      """
      void main() {
        var xs: [bool] = new bool[1]
        while xs {} 
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // For Statements

  @Test
  void basicForStatements() {
    var p =
      """
      void main() {
        var x1: int = 1
        var x2: int = 1000
        var k: bool = false
        for i from 1 to 10 {}
        for i from 0 to 0 {}
        for j from x1 to x2 {}
        for k from 0 to 20 {}   # redefines k for the loop
        for k from k to 10 {}   # type checks, but runtime error
        for i from 0 to 10 { for i from 10 to 20 {} } 
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void illegalBoolInForFrom() {
    var p =
      """
      void main() {
        for i from true to 10 {} 
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalDoubleInForTo() {
    var p =
      """
      void main() {
        for i from 0 to 5.0 {} 
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalNullInForFrom() {
    var p =
      """
      void main() {
        for i from null to 100 {} 
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Function Calls

  @Test
  void basicFunctionCalls() {
    var p =
      """
      int f(x: int) {return x + 1}
      bool g() {return true}
      [string] h(xs: [string], n: int) {return xs}
      void k() {return null}
      void main() {
        var x: int = f(0)
        var y: int = f(x)
        var z: bool = g()
        var xs: [string] = new string[10]
        var ys: [string] = h(xs, x)
        var u: int = k()
        var vs: [bool] = k()
        f(x)
        f(null)
        g()
        h(ys, y)
        f(f(f(x)))
        h(h(h(xs, f(0)), f(1)), f(f(0)) + 1)
        k()
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void callToUndeclaredFunction() {
    var p =
      """
      void main() {
        f()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void callWithTooFewArgs() {
    var p =
      """
      void f(x: int) {}
      void main() {
        f()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void callWithTooManyArgs() {
    var p =
      """
      void f(x: int) {}
      void main() {
        f(1, 2)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void callWithWrongNonArrayArgType() {
    var p =
      """
      void f(x: int, y: bool) {}
      void main() {
        f(1, 2)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void callWithWrongArrayArgType() {
    var p =
      """
      void f(x: int, y: [bool]) {}
      void main() {
        f(1, true)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void callWithWrongArrayArgToNonArrayParamType() {
    var p =
      """
      void f(x: int, y: bool) {}
      void main() {
        var xs: [bool] = null
        f(1, xs)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void callWithWrongNonArrayReturnType() {
    var p =
      """
      int f(x: int, y: bool) {}
      void main() {
        var r: string = f(1, true)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void callWithWrongArrayReturnType() {
    var p =
      """
      [int] f(x: int, y: bool) {}
      void main() {
        var r: int = f(1, true)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void callWithTwoParamsaAndTooFewArgs() {
    var p =
      """
      int f(x: int, y: bool) {}
      void main() {
        var r: int = f(1)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void callWithTwoParamsAndTooManyArgs() {
    var p =
      """
      int f(x: int, y: bool) {}
      void main() {
        var r: int = f(1, true, null)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  //----------------------------------------------------------------------
  // Shadowing and Basic Lifetimes

  @Test
  void basicAllowedShadowing() {
    var p =
      """
      void main() {
        var x = "abc"
        if true {
          var x = 1.0
          var y = x * 0.01
        }
        else if false {
          var x = true
          var y = x and false
        }
        for x from 0 to 10 {
          var y = x / 2
        }
        while true {
          var x = new string[10]
          var y: [string] = x
        }
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }
  
  @Test
  void illegalShadowing() {
    var p =
      """
      void main() {
        var x = 0
        if true {
          var x = 0.0
          var y: double = x + 1.0
        }
        var x = 1
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalLifetimeinElse() {
    var p =
      """
      void main() {
        if true {
          var x = 0.0
          var y: double = x + 1.0
        }
        else {
          var z = x
        }
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalLifetimeAfterWhile() {
    var p =
      """
      void main() {
        while true {
          var x = 0.0
        }
        var y = x
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void illegalLifetimeAfterFor() {
    var p =
      """
      void main() {
        for i from 0 to 10 {
          var x = 0.0
        }
        var y = x        
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Built-in functions

  // print and println
  
  @Test
  void basicPrintCalls() {
    var p =
      """
      void main() {
        print(0)
        println(0)
        print(1.0)
        println(1.0)
        print(true)
        println(true)
        print("abc")
        println("abc")
        print(null)
        println(null)
        # print returns null        
        var x: int = print(0)   
        var y: bool = print(0)  
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void printStructObject() {
    var p =
      """
      struct s {}
      void main() {
        var s1: s = new s()
        print(s1)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void printArrayObject() {
    var p =
      """
      void main() {
        var xs: [int] = new int[10]
        print(xs)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
 @Test
  void printlnStructObject() {
    var p =
      """
      struct s {}
      void main() {
        var s1: s = new s()
        println(s1)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void printlnArrayObject() {
    var p =
      """
      void main() {
        var xs: [int] = new int[10]
        println(xs)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
 
  @Test
  void printTooManyArgs() {
    var p =
      """
      void main() {
        print(0, 1)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void printTooFewArgs() {
    var p =
      """
      void main() {
        print()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void printlnTooManyArgs() {
    var p =
      """
      void main() {
        println(0, 1)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void printlnTooFewArgs() {
    var p =
      """
      void main() {
        println()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  // readln

  @Test
  void basicReadlnCalls() {
    var p =
      """
      void main() {
       readln()
       var x: string = readln()
       var y: string = x + " " + readln()
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void readlnWrongReturnType() {
    var p =
      """
      struct s {}
      void main() {
        var s: int = readln()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void readlnTooManyArgs() {
    var p =
      """
      void main() {
        readln("prompt")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  // casting

  @Test
  void basicCastFunctionCalls() {
    var p =
      """
      void main() {
        var x1: string = str_val(5)
        var y1: string = "" + str_val(5)
        var x2: string = str_val(3.1)
        var y2: string = "" + str_val(3.1)
        var x3: int = int_val("5")
        var y3: int = 0 + int_val("5")
        var x4: int = int_val(3.1)
        var y4: int = 0 + int_val(3.1)
        var x5: double = dbl_val("3.1")
        var y5: double = 0.0 + dbl_val("3.1")
        var x6: double = dbl_val(5)
        var y6: double = 0.0 + dbl_val(5)
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  // str_val calls

  @Test
  void strValWrongReturnType() {
    var p =
      """
      void main() {
        var s: int = str_val(5)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void strValWrongArgType() {
    var p =
      """
      void main() {
        var s: string = str_val("5")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void strValTooManyArgs() {
    var p =
      """
      void main() {
        var s: string = str_val(5, null)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void strValTooFewArgs() {
    var p =
      """
      void main() {
        var s: string = str_val()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  // int_val calls

  @Test
  void intValWrongReturnType() {
    var p =
      """
      void main() {
        var x: string = int_val("5")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void intValWrongArgType() {
    var p =
      """
      void main() {
        var x: int = int_val(5)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void intValTooManyArgs() {
    var p =
      """
      void main() {
        var x: int = int_val("5", null)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void intValTooFewArgs() {
    var p =
      """
      void main() {
        var x: int = int_val()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  // dbl_val calls

  @Test
  void dblValWrongReturnType() {
    var p =
      """
      void main() {
        var x: string = dbl_val("3.1")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void dblValWrongArgType() {
    var p =
      """
      void main() {
        var x: double = dbl_val(3.1)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void dblValTooManyArgs() {
    var p =
      """
      void main() {
        var x: double = dbl_val("3.1", null)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void dblValTooFewArgs() {
    var p =
      """
      void main() {
        var x: double = dbl_val()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  // size

  @Test
  void basicSizeFunctionCalls() {
    var p =
      """
      void main() {
        var x1: int = size("abc")
        var y1: int = 0 + size("abc")
        var x2: int = size(new int[1])
        var y2: int = 0 + size(new int[1])
        var x3: int = size(new string[10])
        var y3: int = 0 + size(new string[10])
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void sizeWrongReturnType() {
    var p =
      """
      void main() {
        var x: string = size("abc")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void sizeWrongArgType() {
    var p =
      """
      void main() {
        var x: int = size(5)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void sizeTooManyArgs() {
    var p =
      """
      void main() {
        var x: int = size("abc", null)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void sizeTooFewArgs() {
    var p =
      """
      void main() {
        var x: int = size()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  // get

  @Test
  void basicGetFunctionCalls() {
    var p =
      """
      struct s {}
      void main() {
        var x1: string = get(0, "abc")
        var y1: string = "" + get(0, "abc")
        var x2: int = get(0, new int[10])
        var y2: int = 0 + get(0, new int[10])
        var x3: double = get(0, new double[10])
        var y3: double = 0.0 + get(0, new double[10])
        var x4: bool = get(0, new bool[10])
        var y4: bool = true and get(0, new bool[10])
        var x5: string = get(0, new string[10])
        var y5: string = "" + get(0, new string[10])
        var x6: s = get(0, new s[10])
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void getWrongReturnType() {
    var p =
      """
      void main() {
        var x: string = get(0, new int[10])
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void getWrongFirstArgType() {
    var p =
      """
      void main() {
        var x: string = get("a", "abc")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void getWrongSecondArgType() {
    var p =
      """
      void main() {
        var x: int = get(0, 1234)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void getTooManyArgs() {
    var p =
      """
      void main() {
        var x: string = get(0, "abc", null)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void getTooFewArgs() {
    var p =
      """
      void main() {
        var x: int = get("abc")
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Assignment statements

  @Test
  void basicAssignmentStatements() {
    var p =
      """
      struct s {}
      void main() {
        # non array
        var x1: int = 0
        var x2: string = ""
        var x3: double = 0.0
        var x4: bool = false
        x1 = 1
        x2 = "abc"
        x3 = 1.0
        x4 = true
        # array
        var x5: [int] = null
        var x6: [string] = null
        var x7: [double] = null
        var x8: [bool] = null
        x5 = new int[10]
        x6 = new string[10]
        x7 = new double[10]
        x8 = new bool[10]
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

    @Test
  void basicAssignmentToNull() {
    var p =
      """
      struct s {}
      void main() {
        var x1: int = 0
        var x2: string = ""
        var x3: double = 0.0
        var x4: bool = false
        x1 = null
        x2 = null
        x3 = null
        x4 = null
        var x5: [int] = new int[10]
        var x6: [string] = new string[10]
        var x7: [double] = new double[10]
        var x8: [bool] = new bool[10]
        x5 = null
        x6 = null
        x7 = null
        x8 = null
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void assignmentWithBaseTypeMismatch() {
    var p =
      """
      void main() {
        var x: int = 0
        var y: double = 0.0
        x = y
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void assignmentWithArrayTypeMismatch() {
    var p =
      """
      void main() {
        var x: [int] = new int[10]
        var y: [double] = new double[10]
        x = y
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void assignmentWithBaseTypeToArrayTypeMismatch() {
    var p =
      """
      void main() {
        var x: int = 10
        var y: [int] = new int[10]
        x = y
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  //----------------------------------------------------------------------
  // Array creation and access

  @Test
  void arrayCreation() {
    var p =
      """
      struct s {}
      void main() {
        var n: int = 10
        var a1: [int] = new int[n]
        var a2: [int] = null
        a2 = a1
        var a3: [double] = new double[10]
        var a4: [string] = new string[n+1]
        var a5: [string] = null
        var a6: [bool] = new bool[n]
        var a7: [s] = new s[n]
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void createArrayWithMismatchedBaseType() {
    var p =
      """
      void main() {
        var x: [int] = new double[10]
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void createArrayWithMismatchedStructType() {
    var p =
      """
      struct s1 {}
      struct s2 {}
      void main() {
        var x: [s2] = new s1[10]
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void arrayAccess() {
    var p =
      """
      struct s {val: string}
      void main() {
        var n: int = 10
        var a1: [bool] = new bool[n]
        var a2: [s] = new s[n]
        var x: bool = a1[n-5]
        a1[0] = x or true
        a2[0] = null
        var s1: s = a2[1]
        var t: string = a2[0].val
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void badArrayAssignment() {
    var p =
      """
      void main() {
        var a1: [bool] = new bool[10]
        a1[0] = 10
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void badArrayAccess() {
    var p =
      """
      void main() {
        var a1: [bool] = new bool[10]
        var x: int = a1[0]
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //----------------------------------------------------------------------
  // Struct creation and access

  @Test
  void basicStructCreation() {
    var p =
      """
      struct s1 {}
      struct s2 {x: int}
      struct s3 {x: int, y: string}
      void main() {
        var p1: s1 = new s1()
        var p2: s2 = new s2(5)
        var p3: s3 = new s3(5, "a")
        var p4: s3 = new s3(null, null)
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void structCreationTooFewArgs() {
    var p =
      """
      struct s {x: int}
      void main() {
        var p1: s = new s()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }
  
  @Test
  void structCreationTooManyArgs() {
    var p =
      """
      struct s {x: int}
      void main() {
        var p1: s = new s(1, 2)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void structCreationWrongArgType() {
    var p =
      """
      struct s {x: int, y: string}
      void main() {
        var p1: s = new s(1, 2)
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void basicStructPathExamples() {
    var p =
      """
      struct s {val: double, t1: t}
      struct t {val: bool, s1: s}
      void main() {
        var p1: s
        var p2: t = new t(null, p1)
        p1 = new s(null, p2)
        p1.val = 1.0
        p2.val = true
        p1.t1.val = false
        p2.s1.val = 2.0
        p1.t1.s1.val = 3.0
        p2.s1.t1.val = true
        var x: double = p1.val
        var y: bool = p2.val
        y = p1.t1.val
        x = p2.s1.val
        x = p1.t1.s1.val
        y = p2.s1.t1.val
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void lvalueWithWrongType() {
    var p =
      """
      struct s {val: double, next: s}
      void main() {
        var p: s = new s(null, null)
        p.next.val = 0
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void rvalueWithCorrectType() {
    var p =
      """
      struct s {val: double, next: s}
      void main() {
        var p: s = new s(null, null)
        var x: double = p.next.next.val
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void rvalueWithWrongType() {
    var p =
      """
      struct s {val: double, next: s}
      void main() {
        var p: s = new s(null, null)
        var x: int = p.next.next.val
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void lvalueWithCorrectType() {
    var p =
      """
      struct s {val: double, next: [s]}
      void main() {
        var p: s = new s(null, null)
        p.next[0].next[1].val = 5.0
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void lvalueWithBadArrayType() {
    var p =
      """
      struct s {val: double, next: [s]}
      void main() {
        var p: s = new s(null, null)
        p.next[0].next[1].next.val = 5.0
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  @Test
  void rvalueWithCorrectArrayType() {
    var p =
      """
      struct s {val: double, next: [s]}
      void main() {
        var p: s = new s(null, null)
        var x: double = p.next[0].next[1].val
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  @Test
  void rvalueWithBadArrayType() {
    var p =
      """
      struct s {val: double, next: [s]}
      void main() {
        var p: s = new s(null, null)
        var x: double = p.next[0].next[1].next.val
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }


  /**************************************************************** */
  //  test cases static files
  /**************************************************************** */
  //These test cases are for me, they are to run the files you gave us, these are the files you wrote
  //I am not trying to claim them as my own, my own test cases are above, these are so I can test tese files. 

  @Test
    void static1() {
      var p =
        """
        #----------------------------------------------------------------------
        # Basic structs and functions
        #----------------------------------------------------------------------

        struct t1 {
          field1: int
        }

        struct t2 {
          field1: double,
          filed2: string
        }

        void f1() {
        }

        int f2() {
        }

        string f3(x: int) {
        }

        bool f4(str: string, index: int) {
        }

        void main() {
        }



        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }

  @Test
    void static2() {
      var p =
        """
        #----------------------------------------------------------------------
        # Basic variable declarations and expressions
        #----------------------------------------------------------------------

        struct T {
        }

        void good_expressions() {

          var t1: T = new T()
          
          # boolean expressions
          var x1: bool = true
          var x2: bool = false
          var e1: bool = null
          var e2: bool = not false
          var e3: bool = not not false
          var e4: bool = not x1
          var e5: bool = x1 and x2
          var e6: bool = (x1 and x2) or (x1 and not x1) or (not x1 and not x2)
          var e7: bool = (t1 == null) or (null == t1) or (null == null) or (null != "foo")
          var e8: bool = (x1 == x2) and (x1 != x2)
          var e9: bool = (1 == 2) or (1.1 == 2.0) or ("a" == "b") or ("a" == "b") or (t1 == t1)
          var e10: bool = (1 != 2) or (1.1 != 2.0) or ("a" != "b") or ("a" != "b") or (t1 != t1)
          var e11: bool = (1 <= 2) and (1.4 > 1.0) and ("a" >= "b") and ("ab" < "bc")

          # arithmetic expressions
          var i1: int = 6 + 3
          var d1: double = 3.14 + 2.1
          var s1: string = "ab" + "cd"
          var i2: int = i1 - i1
          var i3: int = i2 - 5 - 6
          var i4: int = i1 * 3 - (i2 / i3)
          var i5: int = 1 + 2 * 3 / 4
          var d2: double = d1 - d1 * d1 - d1 * 4.117
          var d3: double = 1.0 + 2.0 * 3.0 / 4.0
        }

        void main() {
        }

        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }

    @Test
    void static3() {
      var p =
        """
        #----------------------------------------------------------------------
        # Basic conditionals
        #----------------------------------------------------------------------

        void main() {

          var flag1: bool = true
          var flag2: bool = false

          while true {
          }

          while flag1 {
          }

          while flag2 {
          }

          while true {
            while false {
            }
            while flag1 {
            }
          }

          while flag1 {
            var flag3: bool = true
            while flag3 {
              var flag1: bool = false
            }
          }

          if true {
          }

          if flag1 {
          }

          if flag1 {
            var flag3: bool = false
          }
          else {
            var flag3: bool = true
          }

          if true {
            var flag3 = false
          }
          else if flag2 {
            var flag3 = false
          }
          else {
            var flag3 = false
          }

          if true {
            var flag3 = false
          }
          else if flag2 {
            var flag3 = false
          }
          else if flag1 {
            var flag3 = false
          }
          else {
            var flag3 = false
          }
            
          if true {
            var flag1 = false
            if flag1 {
              var flag2 = true
              if flag2 {
                var flag1 = true
              }
              else {
                var flag1 = true
              }
            }
          }

          while flag1 {
            if false {
              var flag1 = false
            }
            else {
              var flag1 = true
            }
          }

        }
        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }

    @Test
    void static4() {
      var p =
        """
        #----------------------------------------------------------------------
        # basic for loops and assignment statements
        #----------------------------------------------------------------------

        void main() {

          var x = ""
          var y = ""
          var z = 100

          for i from 1 to 10 {
          }

          for i from 1 to 10 {
            var y = 20 * i
          }

          for x from 1 to 10 {
            var y = 10
            var z = 20 + x
          }

          for i from 1 to z {
            var x = 20
            for j from z to z + 10 {
              i = i - 1 + 1
              x = x * j
            }
          }
          
          for x from 1 to 10 {
            for x from 11 to 20 {
              var y = x + 1
            }
          }

        }

        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }

    @Test
    void static5() {
      var p =
        """
        #----------------------------------------------------------------------
        # function calls and return statements
        #----------------------------------------------------------------------

        void f1() {
          var x: int x = 1
          return null
        }

        int f2() {
          var x: int = 1
          return 0
        }

        bool f3() {
          return true
        }

        bool f4() {
          var x: bool = f3()
          return x
        }

        int f5() {
          return f5()
        }

        void f6(x: int) {
          f6(x - 1)
          return null
        }

        void f7(x: int, y: bool) {
          return f7(1, y and f4())
        }

        int f8(x: int, y: string, z: int) {
          f8(f7(1, true), "", 42)
          return null
        }


        void main() {
          print("hello world")
          var x: int = int_val("1")
          var y: double = dbl_val("3.14")
          var z: string = str_val(42)
          var u: string = str_val(3.14159)
          var v: string = get(0, "ab")
          var r: int = size("ab")
          var s1: string = "Please enter " + "your name: "
          println(s1)
          var s2: string = readln()
          s1 = "Hi " + s2
          s1 = s1 + "." + " How " + "are you?"
        }

        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }

    @Test
    void static6() {
      var p =
        """
        #----------------------------------------------------------------------
        # basic functions and structs
        #----------------------------------------------------------------------

        int a(x: int) {
          return x + 1
        }

        int b(y: int) {
          var x = a(y)
          return x
        }

        struct T {
          x: int,
          y: string
        }

        T c(x: int) {
          var t: T = new T(10, null)
          return t
        }

        string d(par1: string, par2: int) {
          if (par2 > 0) and (par2 < 10) and (size(par1) != 0) {
            return par1
          }
          else {
            return str_val(par2)
          }
        }

        void main() {
          d("foo", 1)
        }


        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }
    

    @Test
    void static7() {
      var p =
        """
        #----------------------------------------------------------------------
        # Struct declarations and path expressions
        #----------------------------------------------------------------------

        struct T1 {
        }

        struct T2 {
          x: int
        }

        struct T3 {
          x: int,
          y: string
        }

        struct T4 {
          x: int,
          y: string,
          z: int
        }

        struct T5 {
          x: int,
          t: T4
        }

        struct T6 {
          t1: T4, 
          t2: T5,
          x: int,
          y: int
        }

        struct T7 {
          x: int,
          n: T7
        }

        struct node {
          val: int,
          nxt: node
        }

        void good_paths() {
          var n1: node = new node(0, null)
          var n2: node = new node(1, null)
          var n3: node = n1.nxt
          n1.val = 10
          n1.nxt = n2
          n2.val = 20
          n2.nxt = new node(2, n2)
          n2.nxt.val = n1.val
          n2.nxt.nxt = new node(3, n3)
          n2.nxt.nxt.val = n2.nxt.val
        }


        void main() {
        }


        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }

    @Test
    void static8() {
      var p =
        """
        #----------------------------------------------------------------------
        # More on arrays
        #----------------------------------------------------------------------

        void main() {
          var xs: [int] = new int[100]
          xs[0] = 1
          var ys: [string] = new string[100]
          ys[0] = "1"
          var zs: [double] = new double[50]
          zs[0] = 3.1
          var x: int = xs[99]
          var y: string = ys[99]
          var z: double = zs[49]
          var xs_ref: [int] = xs
          var ys_ref: [string] = ys
          var zs_ref: [double] = zs
        }

        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }

    @Test
    void static9() {
      var p =
        """
        #----------------------------------------------------------------------
        # More involved lvalues
        #----------------------------------------------------------------------

        struct node {
          val: int, 
          children: [node]
        }

        void main() {
          var roots: [node] = new node[20]
          roots[0] = new node(null, null)
          roots[0].val = 10
          roots[0].children = new node[5] #here
          roots[0].children[0] = new node(null, null)
          roots[0].children[0].val = 20
        }

        """;
      new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
    }




  //----------------------------------------------------------------------
  // TODO: Design and implement the following unit tests and add them
  // below. Make sure your added unit tests pass.
  //
  // 1. Two new "positive" tests. Each test should involve an
  //    "interesting" case.
  //
  // 2. Three new "negative" tests. Each test should involve an
  //    "interesting" case.
  //----------------------------------------------------------------------  
  
  //POSITIVE TESTS
  //calling path expr on the rvalue instead of lvalue.. (i didn't code that yet)

  //more rvalue stuff
  @Test
  void doublePath() {
    var p =
      """
      struct s {val: double, t1: t}
      struct t {val: bool, s1: s}
      void main() {
        var p1: s
        var p2: t = new t(null, p1)
        p1 = new s(null, p2)
        p1.val = 1.0
        p2.val = true
        p1.t1.val = p2.val
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }
  //EVEN more rvalue stuff
  @Test
  void nullPath() {
    var p =
      """
      struct s {val: double, t1: t}
      struct t {val: bool, s1: s}
      void main() {
        var p1: s
        var p2: t = new t(null, p1)
        p1 = new s(null, p2)
        p1.val = null
        p2.val = null
        p1.t1.val = null
      }
      """;
    new ASTParser(new Lexer(istream(p))).parse().accept(new SemanticChecker());
  }

  //NEGATIVE TESTS
  //null value in array [null] (causes error)
  @Test
  void badArrayAccessNull() {
    var p =
      """
      struct s {val: double, next: [s]}
      void main() {
        var p: s = new s(null, null)
        var x: double = p.next[null].next[null].val
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //struct with two varibles of same name
  @Test
  void doubleStructParams() {
    var p =
      """
      struct s1 {x: int, x: int}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

  //bad void type put in param
  @Test
  void structVoidTypeParams() {
    var p =
      """
      struct s1 {x: void, y: int}
      void main() {}
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }

}