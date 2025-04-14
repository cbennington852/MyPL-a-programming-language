/**
 * CPSC 326, Spring 2025
 * Unit tests for he Code Generator.
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;


class AnnotationsTests {

  /** For dealing with program output **/
  private PrintStream stdout = System.out;
  private ByteArrayOutputStream output = new ByteArrayOutputStream(); 

  @BeforeEach
  public void changeSystemOut() {
    // redirect System.out to output
    System.setOut(new PrintStream(output));
  }

  @AfterEach
  public void restoreSystemOut() {
    // reset System.out to standard out
    System.setOut(stdout);
  }

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
  
  /**
   * Helper to generate a VM to run
   */
  VM build(String program) {
    Lexer lexer = new Lexer(istream(program));
    ASTParser parser = new ASTParser(lexer);
    Program p = parser.parse();
    p.accept(new SemanticChecker());
    VM vm = new VM();
    p.accept(new CodeGenerator(vm));
    return vm;
  }

  /**
   * Helper to generate a VM to run
   */
  VM debugBuild(String program) {
    Lexer lexer = new Lexer(istream(program));
    ASTParser parser = new ASTParser(lexer);
    Program p = parser.parse();
    p.accept(new SemanticChecker());
    VM vm = new VM();
    vm.debugMode(true);
    p.accept(new CodeGenerator(vm));
    return vm;
  }
  
  
  @Test
  void basicProgram() {
    String p = """
      @VM
      int f1(x: int, y: int) {
        ADD()
        RET()
      }

      void main() {
        print("checking this ")
        var y  = f1(1,3)
        print(y)
      }
    """;
    build(p).run();
    assertEquals("checking this 4", output.toString());    
  }


   @Test
  void parseTest() {
    var p = """
    @VM
    int f1(x: int, y: int) {
      ADD()
      RET()
    }
    
    """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    parser.parse();
  }

  @Test
  void basicProgram2() {
    //For void functions the programer doesn't have to put return, the function can just figure it out. 
    String p = """
      @VM
      void f1() {
        PUSH("cheese")
        WRITE()
        PUSH("booger")
        WRITE()
      }

      void main() {
        f1()
      }
    """;
    build(p).run();
    assertEquals("cheesebooger", output.toString());    
  }

  @Test
  void basicProgramWithComment() {
    //For void functions the programer doesn't have to put return, the function can just figure it out. 
    String p = """
      @VM
      void f1() {
        PUSH("cheese") #adasdasd
        WRITE()
        PUSH("booger")
        WRITE()
      }

      void main() {
        f1()
      }
    """;
    build(p).run();
    assertEquals("cheesebooger", output.toString());    
  }

   @Test
  void basicProgramWithDocs() {
    //For void functions the programer doesn't have to put return, the function can just figure it out. 
    String p = """
      @VM
      /* 
          this function returns true
          @ n : a random number
        */
      void f1() {
        PUSH("cheese") #adasdasd
        WRITE()
        PUSH("booger")
        WRITE()
      }

      void main() {
        f1()
      }
    """;
    build(p).run();
    assertEquals("cheesebooger", output.toString());    
  }

  @Test
  void basicProgramWithDocsVMCallBelow() {
    //For void functions the programer doesn't have to put return, the function can just figure it out. 
    String p = """
      
      /* 
          this function returns true
          @ n : a random number
        */
      @VM
      void f1() {
        PUSH("cheese") #adasdasd
        WRITE()
        PUSH("booger")
        WRITE()
      }

      void main() {
        f1()
      }
    """;
    build(p).run();
    assertEquals("cheesebooger", output.toString());    
  }

  @Test
  void badRetType() {
    String p = """
      @VM
      int f1() {
        PUSH("cheese")
        WRITE()
        PUSH("booger")
        WRITE()
      }

      void main() {
        var x:int = f1()
        print(x)
      }
    """;
    VM vm = build(p);
    build(p).run();
    assertEquals("cheeseboogernull", output.toString());    
  }

  @Test
  void main_VM() {
    String p = """
      @VM
      void main() {
        PUSH("main")
        WRITE()
      }
    """;
    VM vm = build(p);
    build(p).run();
    assertEquals("main", output.toString());    
  }

  
  
  @Test
  void nullworks() {
    String p = """
      @VM
      void main() {
        PUSH(null)
        WRITE()
      }
    """;
    VM vm = build(p);
    build(p).run();
    assertEquals("null", output.toString());    
  }

  @Test
  void varibleTypesWork() {
    String p = """
      @VM
      void main() {
        PUSH(null)
        PUSH(1)
        PUSH(1.1)
        PUSH(true)
        PUSH(false)
        PUSH("straw")
        WRITE()
        WRITE()
        WRITE()
        WRITE()
        WRITE()
        WRITE()
      }
    """;
    VM vm = build(p);
    build(p).run();
    assertEquals("strawfalsetrue1.11null", output.toString());    
  }
  ///////////////////////////////////////////////////////
  // NEGATIVE TESTS
  ///////////////////////////////////////////////////////

  
  @Test
  void expressionWithTooManyParens() {
    var p =
      """
      @VM
      int f1(x: int, y: int) {
        LOAD(0)))
        LOAD(1)
        ADD()
        RET()
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void expressionWithNonVMCode() {
    var p =
      """
      @VM
    int f1(x: int, y: int) {
      x =1 + 3
      LOAD(1)
      ADD()
      RET()
    }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }

  @Test
  void expressionWithNonVMCode2() {
    var p =
      """
      @VM
      int f1(x: int, y: int) {
        return x
        LOAD(1)
        ADD()
        RET()
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    SimpleParser parser = new SimpleParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }


  @Test
  void expressionWithNonVMCodeExpr() {
    var p =
      """
      @VM
      int f1(x: int, y: int) {
        LOAD(1+3)
        ADD()
        RET()
      }
      """;   
    Lexer lexer = new Lexer(istream(p));
    ASTParser parser = new ASTParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));    
  }


  @Test
  void expressionWithBadOperator() {
    var p =
      """
      @VM
      int f1(x: int, y: int) {
        PUSH("cheese")
        WRITE()
        GOON()
      }
      """;
    Lexer lexer = new Lexer(istream(p));
    ASTParser parser = new ASTParser(lexer);
    Exception e = assertThrows(MyPLException.class, () -> parser.parse());
    assertTrue(e.getMessage().startsWith("PARSE_ERROR: "));   
  }

  @Test
  void forgotAt() {
    var p =
      """
      int f1(x: int, y: int) {
        PUSH("cheese")
        WRITE()
      }

      void main() {
        PUSH("main")
        WRITE()
      }
      """;
    Program r = new ASTParser(new Lexer(istream(p))).parse();
    SemanticChecker c = new SemanticChecker();
    Exception e = assertThrows(MyPLException.class, () -> r.accept(c));
    assertTrue(e.getMessage().startsWith("STATIC_ERROR: "));
  }





}